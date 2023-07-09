package pers.ketikai.network.redirect.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * <p>NetUtils</p>
 *
 * <p>Created on 2023/7/9 13:25</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public abstract class NetUtils {

    public static Inet4Address getIPv4(String host) throws UnknownHostException {
        for (InetAddress address : InetAddress.getAllByName(host)) {
            if (address instanceof Inet4Address) {
                return (Inet4Address) address;
            }
        }
        return null;
    }

    public static Inet6Address getIPv6(String host) throws UnknownHostException {
        for (InetAddress address : InetAddress.getAllByName(host)) {
            if (address instanceof Inet6Address) {
                return (Inet6Address) address;
            }
        }
        return null;
    }
}
