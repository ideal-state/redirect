package pers.ketikai.network.redirect.packet;

import java.net.InetSocketAddress;

/**
 * <p>Packet</p>
 *
 * <p>Created on 2023/7/9 5:46</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class Packet {

    public static final int REAL_SERVER = 0;
    public static final int REAL_CLIENT = 1;

    private final int target;
    private final InetSocketAddress address;
    private final byte[] data;

    public Packet(int target, InetSocketAddress address, byte[] data) {
        this.target = target;
        this.address = address;
        this.data = data;
    }

    public int getTarget() {
        return target;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }
}
