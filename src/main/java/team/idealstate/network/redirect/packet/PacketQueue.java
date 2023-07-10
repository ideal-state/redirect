package team.idealstate.network.redirect.packet;

import java.util.Iterator;

/**
 * <p>PacketQueue</p>
 *
 * <p>Created on 2023/7/9 6:49</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public interface PacketQueue {

    void send(Packet packet);

    Packet toRealServer();

    Packet toRealClient();

    Iterator<Packet> allToRealServer();

    Iterator<Packet> allToRealClient();

    void clearToRealServer();

    void clearToRealClient();
}

