package pers.ketikai.network.redirect.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ketikai.network.redirect.packet.Packet;
import pers.ketikai.network.redirect.packet.PacketQueue;
import pers.ketikai.network.redirect.util.BufferUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>DatagramClientPool</p>
 *
 * <p>Created on 2023/7/9 9:54</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class DatagramClientPool {

    private static final Logger log = LoggerFactory.getLogger(DatagramClientPool.class);

    private final Selector selector = Selector.open();
    private final PacketQueue packetQueue;
    private final InetSocketAddress realServer;
    private final Thread cleaner;
    private final Thread sender;
    private final Thread receiver;
    private final AtomicBoolean closed = new AtomicBoolean(true);
    private final AtomicBoolean available = new AtomicBoolean(true);
    private final Lock receiveLock = new ReentrantLock();
    private final Lock sendLock = new ReentrantLock();
    private final Map<InetSocketAddress, DatagramClient> clients = new ConcurrentHashMap<>(32, 0.6F);
    private final Map<DatagramChannel, DatagramClient> clients0 = new ConcurrentHashMap<>(32, 0.6F);

    private final String suffix;

    public DatagramClientPool(PacketQueue packetQueue, InetSocketAddress realServer) throws IOException {
        this.packetQueue = packetQueue;
        this.realServer = realServer;
        suffix = String.valueOf(realServer.getPort());

        this.cleaner = new Thread(() -> {
            boolean tryReceiveLock, trySendLock;
            Iterator<DatagramClient> clientIt;
            DatagramClient client;
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(30L);
                } catch (InterruptedException e) {
                    break;
                }

                tryReceiveLock = receiveLock.tryLock();
                trySendLock = sendLock.tryLock();
                if (!tryReceiveLock || !trySendLock) {
                    if (tryReceiveLock) {
                        receiveLock.unlock();
                    }
                    if (trySendLock) {
                        sendLock.unlock();
                    }
                    continue;
                }

                try {
                    clientIt = clients.values().iterator();
                    while (clientIt.hasNext()) {
                        client = clientIt.next();
                        if (!client.isActive()) {
                            clientIt.remove();
                            client.close();
                        } else {
                            client.fuck();
                        }
                    }
                } finally {
                    receiveLock.unlock();
                    sendLock.unlock();
                }
            }
            log.info("虚拟客户端池清理线程已关闭");
        }, "client-pool-cleaner" + ":" + suffix);

        receiver = new Thread(() -> {
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
            int selectNow;
            Iterator<SelectionKey> keys;
            SelectionKey key;
            byte[] data, newData, temp;
            DatagramChannel channel;
            int dataLen;
            InetSocketAddress realServer0;
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.NANOSECONDS.sleep(1000L);
                } catch (InterruptedException e) {
                    break;
                }
                selectNow = 0;
                try {
                    selectNow = selector.selectNow();
                } catch (ClosedSelectorException ignored) {
                } catch (IOException e) {
                    log.error("虚拟客户端池在接收数据时抛出选择器异常", e);
                }
                if (selectNow > 0) {
                    keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        key = keys.next();
                        keys.remove();
                        if (key.isValid() && key.isReadable()) {
                            data = (byte[]) key.attachment();
                            channel = (DatagramChannel) key.channel();
                            if (receiveLock.tryLock()) {
                                try {
                                    realServer0 = (InetSocketAddress) channel.receive(byteBuffer);
                                    newData = BufferUtils.copyToBytes(byteBuffer);
                                    if (data != null) {
                                        dataLen = data.length;
                                        temp = new byte[data.length + newData.length];
                                        System.arraycopy(data, 0, temp, 0, dataLen);
                                        System.arraycopy(newData, 0, temp, dataLen, newData.length);
                                        newData = temp;
                                    }
                                    if (realServer0 == null) {
                                        key.attach(newData);
                                    } else {
                                        key.attach(null);
                                        packetQueue.send(new Packet(Packet.REAL_CLIENT,
                                                clients0.get(channel).getRealClient(), newData));
                                    }
                                } catch (ClosedChannelException ignored) {
                                } catch (IOException e) {
                                    log.error("一个虚拟客户端在接收数据时抛出异常", e);
                                } finally {
                                    receiveLock.unlock();
                                }
                            }
                            byteBuffer.clear();
                        }
                    }
                }
            }
            log.info("虚拟客户端池接收线程已关闭");
        }, "client-pool-receiver" + ":" + suffix);

        this.sender = new Thread(() -> {
            Packet packet;
            InetSocketAddress realClient;
            DatagramClient client;
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.NANOSECONDS.sleep(1000L);
                } catch (InterruptedException e) {
                    break;
                }
                if ((packet = packetQueue.toRealServer()) != null) {
                    realClient = packet.getAddress();
                    if (sendLock.tryLock()) {
                        try {
                            if ((client = clients.get(realClient)) == null) {
                                clients.put(realClient, (client = new DatagramClient(realServer, realClient, packetQueue, selector)));
                                clients0.put(client.getChannel(), client);
                            }
                            client.send(packet);
                        } catch (IOException e) {
                            log.error("新建虚拟客户端时抛出异常", e);
                        } finally {
                            sendLock.unlock();
                        }
                    }
                }
            }
            log.info("虚拟客户端池发送线程已关闭");
        }, "client-pool-sender" + ":" + suffix);
    }

    public void start() {
        if (isAvailable() && closed.getAndSet(false)) {
            log.info(suffix + " - " + "正在启动虚拟客户端池...");
            cleaner.start();
            receiver.start();
            sender.start();
        }
        log.info(suffix + " - " + "虚拟客户端池已启动");
    }

    public void shutdown() {
        if (!closed.getAndSet(true)) {
            log.info(suffix + " - " + "正在中断虚拟客户端池工作线程...");
            cleaner.interrupt();
            receiver.interrupt();
            sender.interrupt();
//            executor.shutdownNow();
            packetQueue.clearToRealServer();
            for (DatagramClient client : clients.values()) {
                if (!client.isClosed()) {
                    client.close();
                }
            }
            clients.clear();
            do {
                try {
                    TimeUnit.MILLISECONDS.sleep(300L);
                } catch (InterruptedException ignored) {
                }
            } while (cleaner.isAlive() || receiver.isAlive() || sender.isAlive());
        }
        log.info(suffix + " - " + "虚拟客户端池已关机");
    }

    public void close() {
        if (available.getAndSet(false)) {
            shutdown();
        }
        log.info(suffix + " - " + "虚拟客户端池已关闭");
    }

    public boolean isAvailable() {
        return available.get();
    }
}
