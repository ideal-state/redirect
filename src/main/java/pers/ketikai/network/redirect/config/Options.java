package pers.ketikai.network.redirect.config;

import pers.ketikai.network.redirect.util.NetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>Options</p>
 *
 * <p>Created on 2023/7/9 16:22</p>
 *
 * @author ketikai
 * @since 1.0.0
 */
public class Options {

    private final List<Integer> ports = new ArrayList<>(16);
    private String deviceName;
    private String srcProto;
    private String distProto;
    private String srcHost;
    private String distHost;
    private Boolean logRaw;

    public String getDeviceName() {
        mustBeNotNull(deviceName);
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        mustBeNull(this.deviceName);
        this.deviceName = checkDeviceName(deviceName);
    }

    private String checkDeviceName(String deviceName) {
        if (deviceName == null || deviceName.isEmpty() || deviceName.isBlank()) {
            throw new IllegalArgumentException("网卡名称不能为空");
        }
        return deviceName;
    }

    public String getSrcProto() {
        mustBeNotNull(srcProto);
        return srcProto;
    }

    public void setSrcProto(String srcProto) {
        mustBeNull(this.srcProto);
        this.srcProto = checkProto(srcProto);
    }

    public String getDistProto() {
        mustBeNotNull(distProto);
        return distProto;
    }

    public void setDistProto(String distProto) {
        mustBeNull(this.distProto);
        this.distProto = checkProto(distProto);
    }

    private String checkProto(String proto) {
        if ("v4".equalsIgnoreCase(proto) || "v6".equalsIgnoreCase(proto)) {
            return proto.toLowerCase();
        }
        throw new IllegalArgumentException(proto);
    }

    public String getSrcHost() {
        mustBeNotNull(srcHost);
        return srcHost;
    }

    public void setSrcHost(String srcHost) throws UnknownHostException {
        mustBeNull(this.srcHost);
        InetAddress.getByName(srcHost);
        this.srcHost = srcHost;
    }

    public InetAddress getSrcAddress() throws UnknownHostException {
        if ("v4".equals(getSrcProto())) {
            return NetUtils.getIPv4(getSrcHost());
        }
        return NetUtils.getIPv6(getSrcHost());
    }

    public String getDistHost() {
        mustBeNotNull(distHost);
        return distHost;
    }

    public void setDistHost(String distHost) throws UnknownHostException {
        mustBeNull(this.distHost);
        InetAddress.getByName(distHost);
        this.distHost = distHost;
    }

    public InetAddress getDistAddress() throws UnknownHostException {
        if ("v4".equals(getDistProto())) {
            return NetUtils.getIPv4(getDistHost());
        }
        return NetUtils.getIPv6(getDistHost());
    }

    public boolean isLogRaw() {
        mustBeNotNull(logRaw);
        return logRaw;
    }

    public void setLogRaw(String logRaw) {
        mustBeNull(this.logRaw);
        this.logRaw = Boolean.parseBoolean(logRaw);
    }

    public List<Integer> getPorts() {
        mustBeNotNull(this.ports.isEmpty() ? null : "");
        return new ArrayList<>(ports);
    }

    public void setPorts(String ports) {
        mustBeNull(this.ports.isEmpty() ? null : "");
        final String[] arr = ports.split(",");
        String[] ss;
        int len, start, end, port;
        final Set<Integer> temp = new LinkedHashSet<>(16, 0.6F);
        for (String s : arr) {
            ss = s.split("-");
            len = ss.length;
            if (len == 1) {
                port = Integer.parseInt(s);
                checkPort(port);
                temp.add(port);
                continue;
            }
            start = Integer.parseInt(ss[0]);
            end = Integer.parseInt(ss[len - 1]);
            if (start > end) {
                start += end;
                end = start - end;
                start -= end;
            }
            for (; start <= end; start++) {
                checkPort(start);
                temp.add(start);
            }
        }
        this.ports.addAll(temp);
    }

    private void checkPort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("端口: " + port);
        }
    }

    private void mustBeNull(Object obj) {
        if (obj != null) {
            throw new IllegalArgumentException("不允许出现重复的参数配置");
        }
    }

    private void mustBeNotNull(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("无效的参数");
        }
    }
}
