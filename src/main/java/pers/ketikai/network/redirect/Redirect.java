package pers.ketikai.network.redirect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.ketikai.network.redirect.client.DatagramClientPool;
import pers.ketikai.network.redirect.config.Options;
import pers.ketikai.network.redirect.packet.PacketQueue;
import pers.ketikai.network.redirect.server.DatagramServer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * <p>Redirect</p>
 *
 * <p>Created on 2023/6/14 7:48</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public final class Redirect {

    private static final Logger log = LoggerFactory.getLogger(Redirect.class);
    private static final Options OPTIONS = new Options();
    private static boolean LOG_RAW = false;

    public static void main(String[] args) throws Exception {
        final RuntimeException optionsEx = new RuntimeException("参数配置不合法, 程序退出!");
        String[] entry;
        for (String arg : args) {
            entry = arg.split("=");
            if (entry.length != 2) {
                throw optionsEx;
            }
            setOption(entry[0], entry[1]);
        }

        LOG_RAW = OPTIONS.isLogRaw();
        flushdns();
        updateDevice(OPTIONS.getDeviceName(), OPTIONS.getSrcAddress());

        final List<Redirecter> redirecters = new ArrayList<>(16);
        for (Integer port : OPTIONS.getPorts()) {
            redirecters.add(Redirecter.create(OPTIONS.getSrcAddress(), OPTIONS.getDistAddress(), port));
        }

        start:
        {
            for (Redirecter redirecter : redirecters) {
                try {
                    redirecter.start();
                } catch (Exception e) {
                    break start;
                }
            }

            final Scanner scanner = new Scanner(System.in);
            main:
            while (scanner.hasNextLine()) {
                switch (scanner.nextLine()) {
                    case "quit":
                    case "exit":
                    case "stop":
                    case "shutdown":
                        break main;
                }
                TimeUnit.SECONDS.sleep(1L);
            }
        }

        for (Redirecter redirecter : redirecters) {
            try {
                redirecter.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static void setOption(String key, String value) {
        try {
            String first = String.valueOf(key.charAt(0));
            final Method method = Options.class.getDeclaredMethod(
                    "set" + key.replaceFirst(first, first.toUpperCase()), String.class);
            method.invoke(OPTIONS, value);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException("非法的参数配置 [key: " + key + " - value: " + value + "]");
        }
    }

    public static void receiveInfo(Logger log, byte[] data) {
        log.info("[receive ->] " + (LOG_RAW ? Arrays.toString(data) : new String(data)));
    }

    public static void sendInfo(Logger log, byte[] data) {
        log.info("[<- send] " + (LOG_RAW ? Arrays.toString(data) : new String(data)));
    }

    private static void flushdns() {
        final String flushCmd = "cmd /c ipconfig /flushdns";
        try {
            final Process process = Runtime.getRuntime().exec(flushCmd);
            if (!process.waitFor(10L, TimeUnit.SECONDS)) {
                throw new RuntimeException("未能刷新 DNS 缓存");
            } else {
                process.destroy();
                TimeUnit.SECONDS.sleep(5L);
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateDevice(String deviceName, InetAddress inetAddress) {
        try {
            final String flushCmd;
            if (inetAddress instanceof Inet4Address) {
                flushCmd = "cmd /c netsh interface ip set address name=\"" + deviceName + "\" source=static " + inetAddress.getHostAddress() + " 255.255.255.0";
            } else {
                flushCmd = "cmd /c netsh interface ipv6 set address name=\"" + deviceName + "\" source=static " + inetAddress.getHostAddress();
            }
            final Process process = Runtime.getRuntime().exec(flushCmd);
            if (!process.waitFor(10L, TimeUnit.SECONDS)) {
                throw new RuntimeException("未能刷新网卡配置");
            } else {
                process.destroy();
                TimeUnit.SECONDS.sleep(5L);
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final class Redirecter {

        private final DatagramServer server;
        private final DatagramClientPool clientPool;

        private Redirecter(DatagramServer server, DatagramClientPool clientPool) {
            this.server = server;
            this.clientPool = clientPool;
        }

        private static Redirecter create(InetAddress bindAddress, InetAddress distAddress, int port) throws Exception {
            final PacketQueue packetQueue = new PacketQueue();
            final InetSocketAddress bindAddress0 = new InetSocketAddress(bindAddress, port);
            final InetSocketAddress distAddress0 =
                    new InetSocketAddress(distAddress, port);
            final DatagramServer server = new DatagramServer(
                    bindAddress0, packetQueue);
            final DatagramClientPool clientPool =
                    new DatagramClientPool(packetQueue, distAddress0);
            return new Redirecter(server, clientPool);
        }

        private void start() {
            server.start();
            clientPool.start();
        }

        private void close() {
            server.close();
            clientPool.close();
        }
    }
}
