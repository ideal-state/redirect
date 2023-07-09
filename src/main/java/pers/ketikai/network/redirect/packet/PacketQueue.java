package pers.ketikai.network.redirect.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>PacketQueue</p>
 *
 * <p>Created on 2023/7/9 6:49</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class PacketQueue {

    private static final Logger log = LoggerFactory.getLogger(PacketQueue.class);

    private final Queue<Packet> toRealServer = new ConcurrentLinkedQueue<>();
    private final Queue<Packet> toRealClient = new ConcurrentLinkedQueue<>();

    public void send(Packet packet) {
        final boolean toServer = packet.getTarget() == Packet.REAL_SERVER;
        try {
            if (toServer) {
                toRealServer.add(packet);
            } else {
                toRealClient.add(packet);
            }
        } catch (IllegalStateException e) {
            if (toServer) {
                log.error("无法添加任务，待发送到真实服务端的数据包队列已满");
            } else {
                log.error("无法添加任务，待发送到真实客户端的数据包队列已满");
            }
        }
    }

    public Packet toRealServer() {
        if (toRealServer.size() == 0) {
            return null;
        }
        return toRealServer.poll();
    }

    public Packet toRealClient() {
        if (toRealClient.size() == 0) {
            return null;
        }
        return toRealClient.poll();
    }

    public void clearToRealServer() {
        toRealServer.clear();
    }

    public void clearToRealClient() {
        toRealClient.clear();
    }
}