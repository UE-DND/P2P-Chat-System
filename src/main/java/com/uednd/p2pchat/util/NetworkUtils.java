package com.uednd.p2pchat.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络基础工具
 * <p>
 * 用于获取本机主机名、IP地址和检查端口状态
 * 
 * @version 1.0.0
 * @since 2025-06-08
 */
public final class NetworkUtils {

    public enum PortStatus {
        AVAILABLE,
        IN_USE,
        INVALID_RANGE
    }

    /**
     * 获取本机主机名
     * @return 主机名, 如果获取失败则返回 null
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }

    /**
     * 获取本机IP地址
     * @return 本机IP地址
     */
    public static String getLocalIpAddress() {
        /*
        由于使用getHostAddress每次都返回环回地址"127.0.0.1"，可能是我电脑中安装了虚拟网卡的原因，此处只能用求教AI解决了。以下是原始代码：
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                return "127.0.0.1";
            }
        AI似乎遍历了电脑中的所有网络接口，然后进行筛选。
        */
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            // .getNetworkInterfaces() Returns:an Enumeration of NetworkInterfaces found on this machine，底层API本身返回 Enumeration，不能用List
            while (networkInterfaces.hasMoreElements()) {  // 判断还有没有下一个元素，应该是防止越界的
                NetworkInterface internet_face = networkInterfaces.nextElement();
                if (internet_face.isLoopback() || !internet_face.isUp() || internet_face.isVirtual())
                    continue; // 跳过非活动、环回和虚拟接口
                Enumeration<InetAddress> addresses = internet_face.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address instanceof Inet4Address) {
                        return address.getHostAddress();  // 判断是否为IPv4地址，找到了直接返回
                    }
                }
            }
            return InetAddress.getLocalHost().getHostAddress();  // 如果没找到，再尝试返回本地主机地址
        } catch (SocketException | UnknownHostException e) {
            return "127.0.0.1";  // 返回环回地址
        }
    }

    /**
     * 检查端口的详细状态
     * @param port 端口号
     * @return 端口状态 (AVAILABLE, IN_USE, INVALID_RANGE)
     */
    public static PortStatus checkPort(int port) {
        if (port < 1 || port > 65535) {
            return PortStatus.INVALID_RANGE;  // 超出端口范围
        }
        try (ServerSocket tryPort = new ServerSocket(port)) {  // 监听port
            return PortStatus.AVAILABLE;  // 监听成功，端口可用
        } catch (IOException e) {
            return PortStatus.IN_USE;  // 端口可能被占用
        }
    }
} 
