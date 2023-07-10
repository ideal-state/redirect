package team.idealstate.network.redirect.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.idealstate.network.redirect.Redirect;
import team.idealstate.network.redirect.packet.Packet;
import team.idealstate.network.redirect.packet.PacketQueue;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>DatagramClient</p>
 *
 * <p>Created on 2023/7/9 8:13</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class DatagramClient {

    private static final Logger log = LoggerFactory.getLogger(DatagramClient.class);

    private final InetSocketAddress realServer;
    private final InetSocketAddress realClient;
    private final DatagramChannel channel;
    private final PacketQueue packetQueue;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public DatagramClient(
            InetSocketAddress realServer, InetSocketAddress realClient, PacketQueue packetQueue,
            Selector selector) throws IOException {
        this.realServer = realServer;
        this.realClient = realClient;
        this.packetQueue = packetQueue;
        this.channel = DatagramChannel.open(
                realServer.getAddress() instanceof Inet4Address ? StandardProtocolFamily.INET : StandardProtocolFamily.INET6);
        this.channel.configureBlocking(false);
        this.channel.register(selector, SelectionKey.OP_READ);
    }

    public void send(Packet packet) {
        if (!closed.get()) {
            active.set(true);
            try {
                Redirect.sendInfo(log, packet.getData());
                channel.send(ByteBuffer.wrap(packet.getData()), realServer);
            } catch (ClosedChannelException ignored) {
            } catch (IOException e) {
                log.error("虚拟客户端在发送数据时抛出异常", e);
            }
        }
    }

    public void close() {
        if (closed.getAndSet(true)) {
            try {
                channel.close();
            } catch (IOException e) {
                log.error("虚拟客户端在关闭频道时抛出异常", e);
            }
        }
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public InetSocketAddress getRealClient() {
        return realClient;
    }

    public boolean isActive() {
        return active.get();
    }

    public boolean isClosed() {
        return closed.get();
    }

    public void fuck() {
        active.set(false);
    }
}
