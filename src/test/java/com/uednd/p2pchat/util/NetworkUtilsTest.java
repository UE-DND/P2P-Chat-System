package com.uednd.p2pchat.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.net.ServerSocket;
import java.io.IOException;

/**
 * NetworkUtils 类的单元测试
 * 
 * @version 1.0.0
 * @since 2025-06-10
 */
public class NetworkUtilsTest {

    /**
     * 测试获取主机名方法
     */
    @Test
    public void testGetHostname() {
        // 获取主机名
        String hostname = NetworkUtils.getHostname();
        
        // 主机名不应该为 null（在大多数正常环境中）
        // 注意：这个测试假设测试环境能够正确解析主机名
        assertNotNull(hostname);
        
        // 主机名不应该为空字符串
        assertFalse(hostname.isEmpty());
    }
    
    /**
     * 测试获取本地 IP 地址方法
     */
    @Test
    public void testGetLocalIpAddress() {
        // 获取本地 IP 地址
        String ipAddress = NetworkUtils.getLocalIpAddress();
        
        // IP 地址不应该为 null
        assertNotNull(ipAddress);
        
        // IP 地址不应该为空字符串
        assertFalse(ipAddress.isEmpty());

        System.out.println("本地 IP 地址: " + ipAddress);
    }
    
    /**
     * 测试端口检查方法 - 无效范围
     */
    @Test
    public void testCheckPortInvalidRange() {
        // 测试小于最小有效端口的值
        assertEquals(NetworkUtils.PortStatus.INVALID_RANGE, NetworkUtils.checkPort(0));
        
        // 测试大于最大有效端口的值
        assertEquals(NetworkUtils.PortStatus.INVALID_RANGE, NetworkUtils.checkPort(65536));
    }
    
    /**
     * 测试端口检查方法 - 可用端口
     * 注意：这个测试会尝试打开一个可用端口，然后验证 checkPort 方法能否正确识别它为可用
     */
    @Test
    public void testCheckPortAvailable() throws IOException {
        // 查找一个可用端口（让系统分配）
        int availablePort;
        try (ServerSocket socket = new ServerSocket(0)) {
            availablePort = socket.getLocalPort();
        }
        
        // 验证 checkPort 方法能否正确识别这个端口为可用
        // 注意：这个测试假设在获取端口和检查之间，没有其他进程占用这个端口
        assertEquals(NetworkUtils.PortStatus.AVAILABLE, NetworkUtils.checkPort(availablePort));
    }
    
    /**
     * 测试端口检查方法 - 已使用端口
     * 注意：这个测试会占用一个端口，然后验证 checkPort 方法能否正确识别它为已使用
     */
    @Test
    public void testCheckPortInUse() throws IOException {
        // 占用一个端口
        int inUsePort;
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            inUsePort = socket.getLocalPort();
            
            // 验证 checkPort 方法能否正确识别这个端口为已使用
            assertEquals(NetworkUtils.PortStatus.IN_USE, NetworkUtils.checkPort(inUsePort));
        } finally {
            // 确保关闭 socket，释放端口
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }
} 