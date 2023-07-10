package team.idealstate.network.redirect.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>ConcurrentLinkedPacketQueue</p>
 *
 * <p>Created on 2023/7/10 18:34</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class ConcurrentLinkedPacketQueue implements PacketQueue {

    private static final Logger log = LoggerFactory.getLogger(ConcurrentLinkedPacketQueue.class);

    private final Queue<Packet> toRealServer = new ConcurrentLinkedQueue<>();
    private final Queue<Packet> toRealClient = new ConcurrentLinkedQueue<>();

    @Override
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

    @Override
    public Packet toRealServer() {
        if (toRealServer.size() == 0) {
            return null;
        }
        return toRealServer.poll();
    }

    @Override
    public Packet toRealClient() {
        if (toRealClient.size() == 0) {
            return null;
        }
        return toRealClient.poll();
    }

    @Override
    public Iterator<Packet> allToRealServer() {
        if (toRealServer.size() == 0) {
            return Collections.emptyListIterator();
        }
        return toRealServer.iterator();
    }

    @Override
    public Iterator<Packet> allToRealClient() {
        if (toRealClient.size() == 0) {
            return Collections.emptyListIterator();
        }
        return toRealClient.iterator();
    }

    @Override
    public void clearToRealServer() {
        toRealServer.clear();
    }

    @Override
    public void clearToRealClient() {
        toRealClient.clear();
    }
}
