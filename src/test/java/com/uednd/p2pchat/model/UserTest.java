package com.uednd.p2pchat.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * User 类的单元测试
 * 
 * @version 1.0.0
 * @since 2025-06-10
 */
public class UserTest {

    /**
     * 测试 User 构造函数和 getter 方法
     */
    @Test
    public void testUserConstructorAndGetters() {
        // 准备测试数据
        String username = "testUser";
        String ipAddress = "192.168.1.1";
        int port = 8080;
        
        // 创建 User 实例
        User user = new User(username, ipAddress, port);
        
        // 验证 getter 方法返回的值与构造函数中提供的值相匹配
        assertEquals(username, user.getUsername());
        assertEquals(ipAddress, user.getIpAddress());
        assertEquals(port, user.getPort());
    }
    
    /**
     * 测试 User setter 方法
     */
    @Test
    public void testUserSetters() {
        // 创建 User 实例，使用初始值
        User user = new User("initialUser", "127.0.0.1", 9090);
        
        // 准备新的测试数据
        String newUsername = "updatedUser";
        String newIpAddress = "10.0.0.1";
        int newPort = 7070;
        
        // 使用 setter 方法更新属性
        user.setUsername(newUsername);
        user.setIpAddress(newIpAddress);
        user.setPort(newPort);
        
        // 验证 getter 方法返回更新后的值
        assertEquals(newUsername, user.getUsername());
        assertEquals(newIpAddress, user.getIpAddress());
        assertEquals(newPort, user.getPort());
    }
    
    /**
     * 测试 User 的 toString 方法
     */
    @Test
    public void testToString() {
        // 准备测试数据
        String username = "testUser";
        String ipAddress = "192.168.1.1";
        int port = 8080;
        
        // 创建 User 实例
        User user = new User(username, ipAddress, port);
        
        // 预期的 toString 输出格式
        String expected = username + " (" + ipAddress + ":" + port + ")";
        
        // 验证 toString 方法的输出
        assertEquals(expected, user.toString());
    }
} 