package pers.ketikai.network.redirect.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ketikai.network.redirect.Redirect;
import pers.ketikai.network.redirect.packet.Packet;
import pers.ketikai.network.redirect.packet.PacketQueue;
import pers.ketikai.network.redirect.util.BufferUtils;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>DatagramServer</p>
 *
 * <p>Created on 2023/7/9 5:31</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class DatagramServer {

    private static final Logger log = LoggerFactory.getLogger(DatagramServer.class);

    private final Selector selector = Selector.open();
    private final DatagramChannel channel;
    private final PacketQueue packetQueue;
    private final AtomicBoolean closed = new AtomicBoolean(true);
    private final AtomicBoolean available = new AtomicBoolean(true);
    private final Thread receiver;
    private final Thread sender;

    private final String suffix;


    public DatagramServer(InetSocketAddress bindAddress, PacketQueue packetQueue) throws IOException {
        this.packetQueue = packetQueue;
        this.channel = DatagramChannel.open(
                bindAddress.getAddress() instanceof Inet4Address ? StandardProtocolFamily.INET : StandardProtocolFamily.INET6);
        this.channel.bind(bindAddress);
        this.channel.configureBlocking(false);
        this.channel.register(selector, SelectionKey.OP_READ);

        suffix = String.valueOf(bindAddress.getPort());

        receiver = new Thread(() -> {
            final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4096);
            int selectNow;
            Iterator<SelectionKey> keys;
            SelectionKey key;
            byte[] data, newData, temp;
            DatagramChannel channel;
            int dataLen;
            InetSocketAddress realClient;
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(5L);
                } catch (InterruptedException e) {
                    break;
                }
                selectNow = 0;
                try {
                    selectNow = selector.selectNow();
                } catch (ClosedSelectorException ignored) {
                } catch (IOException e) {
                    log.error("虚拟服务端在接收数据时抛出选择器异常", e);
                }
                if (selectNow > 0) {
                    keys = selector.selectedKeys().iterator();
                    while (keys.hasNext()) {
                        key = keys.next();
                        keys.remove();
                        if (key.isValid() && key.isReadable()) {
                            data = (byte[]) key.attachment();
                            channel = (DatagramChannel) key.channel();
                            try {
                                realClient = (InetSocketAddress) channel.receive(byteBuffer);
                                newData = BufferUtils.copyToBytes(byteBuffer);
                                if (data != null) {
                                    dataLen = data.length;
                                    temp = new byte[data.length + newData.length];
                                    System.arraycopy(data, 0, temp, 0, dataLen);
                                    System.arraycopy(newData, 0, temp, dataLen, newData.length);
                                    newData = temp;
                                }
                                if (realClient == null) {
                                    key.attach(newData);
                                } else {
                                    key.attach(null);
                                    packetQueue.send(new Packet(Packet.REAL_SERVER, realClient, newData));
                                }
                            } catch (ClosedChannelException ignored) {
                            } catch (IOException e) {
                                log.error("虚拟服务端在接收数据时抛出异常", e);
                            }
                            byteBuffer.clear();
                        }
                    }
                }
            }
            log.info("虚拟服务端接收线程已关闭");
        }, "server-receiver" + ":" + suffix);

        sender = new Thread(() -> {
            Packet packet;
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(5L);
                } catch (InterruptedException e) {
                    break;
                }
                if ((packet = packetQueue.toRealClient()) != null) {
                    try {
                        Redirect.receiveInfo(log, packet.getData());
                        channel.send(ByteBuffer.wrap(packet.getData()), packet.getAddress());
                    } catch (ClosedChannelException ignored) {
                    } catch (IOException e) {
                        log.error("虚拟服务端在发送数据时抛出异常", e);
                    }
                }
            }
            log.info("虚拟服务端发送线程已关闭");
        }, "server-sender" + ":" + suffix);
    }

    public void start() {
        if (isAvailable() && closed.getAndSet(false)) {
            log.info(suffix + " - " + "正在启动虚拟服务端...");
            receiver.start();
            sender.start();
        }
        log.info(suffix + " - " + "虚拟服务端已启动");
    }

    public void shutdown() {
        if (!closed.getAndSet(true)) {
            log.info(suffix + " - " + "正在中断虚拟服务端频道线程...");
            receiver.interrupt();
            sender.interrupt();
            packetQueue.clearToRealClient();
            do {
                try {
                    TimeUnit.MILLISECONDS.sleep(300L);
                } catch (InterruptedException ignored) {
                }
            } while (receiver.isAlive() || sender.isAlive());
        }
        log.info(suffix + " - " + "虚拟服务端已关机");
    }

    public void close() {
        if (available.getAndSet(false)) {
            shutdown();
            log.info(suffix + " - " + "正在关闭子组件...");
            try {
                channel.close();
            } catch (IOException e) {
                log.error(suffix + " - " + "虚拟服务端在关闭频道时抛出异常", e);
            }
        }
        log.info(suffix + " - " + "虚拟服务端已关闭");
    }

    public boolean isAvailable() {
        return available.get();
    }
}
