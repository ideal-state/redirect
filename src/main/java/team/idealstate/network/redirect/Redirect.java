package team.idealstate.network.redirect;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.idealstate.network.redirect.client.DatagramClientPool;
import team.idealstate.network.redirect.config.Options;
import team.idealstate.network.redirect.packet.ConcurrentLinkedPacketQueue;
import team.idealstate.network.redirect.packet.PacketQueue;
import team.idealstate.network.redirect.server.DatagramServer;
import team.idealstate.network.redirect.util.JarUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.*;
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

    static {
        loadLog4j2XML();
    }

    private static final Logger log = LoggerFactory.getLogger(Redirect.class);
    private static final Options OPTIONS = new Options();
    private static boolean LOG_RAW = false;

    private static void loadLog4j2XML() {
        final File file = new File("./log4j2.xml");
        try {
            if (!file.exists()) {
                JarUtils.copy(Redirect.class, "/log4j2.xml", new File("."));
            }
            Configurator.initialize(
                    null,
                    new ConfigurationSource(
                            new FileInputStream("./log4j2.xml")
                    )
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean loadConfig() throws IOException {
        final File file = new File("./redirect.properties");
        if (file.exists()) {
            final Properties properties = new Properties();
            try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                properties.load(inputStream);
            }
            properties.forEach((key, value) -> {
                setOption(String.valueOf(key), String.valueOf(value));
            });
            return true;
        }
        JarUtils.copy(Redirect.class, "/redirect.properties", new File("."));
        return false;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1 || !"start".equals(args[0])) {
            throw new RuntimeException("启动失败, 此次操作可能是误启动, 正式的启动参数仅能有一个且必须是 start");
        }

        if (!loadConfig()) {
            log.info("没有找到配置文件，视为首次启动，已生成默认配置。\n" +
                    "程序将在 3s 后自动退出，请在配置修改完成后，再尝试启动！");
            TimeUnit.SECONDS.sleep(3L);
            return;
        }

        LOG_RAW = OPTIONS.isLogRaw();
        flushdns();
        updateDevice(OPTIONS.getVirtualDeviceName(), OPTIONS.getSrcAddress());

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
        log.info("flushdns... wait for 5s");
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
        log.info("update device... wait for 5s");
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
            final PacketQueue packetQueue = new ConcurrentLinkedPacketQueue();
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
