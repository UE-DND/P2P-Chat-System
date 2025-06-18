package com.uednd.p2pchat.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Message 类的单元测试
 * 
 * @version 1.0.0
 * @since 2025-06-10
 */
public class MessageTest {

    /**
     * 测试空构造函数
     */
    @Test
    public void testEmptyConstructor() {
        // 创建 Message 实例
        Message message = new Message();
        
        // 验证所有字段初始值为 null
        assertNull(message.getSender());
        assertNull(message.getReceiver());
        assertNull(message.getContent());
        assertNull(message.getType());
        assertNull(message.getFilePath());
    }

    /**
     * 测试文本消息构造函数和 getter 方法
     */
    @Test
    public void testTextMessageConstructorAndGetters() {
        // 准备测试数据
        String sender = "Alice";
        String receiver = "Bob";
        String content = "Hello, Bob!";
        
        // 创建文本消息
        Message message = new Message(sender, receiver, content);
        
        // 验证 getter 方法返回的值与构造函数中提供的值相匹配
        assertEquals(sender, message.getSender());
        assertEquals(receiver, message.getReceiver());
        assertEquals(content, message.getContent());
        assertEquals("TEXT", message.getType());
        assertNull(message.getFilePath());
    }
    
    /**
     * 测试文件消息构造函数和 getter 方法
     */
    @Test
    public void testFileMessageConstructorAndGetters() {
        // 准备测试数据
        String sender = "Alice";
        String receiver = "Bob";
        String content = "图片文件";
        String filePath = "/path/to/image.jpg";
        
        // 创建文件消息
        Message message = new Message(sender, receiver, content, filePath);
        
        // 验证 getter 方法返回的值与构造函数中提供的值相匹配
        assertEquals(sender, message.getSender());
        assertEquals(receiver, message.getReceiver());
        assertEquals(content, message.getContent());
        assertEquals("FILE", message.getType());
        assertEquals(filePath, message.getFilePath());
    }
    
    /**
     * 测试 setter 方法
     */
    @Test
    public void testSetters() {
        // 创建空消息
        Message message = new Message();
        
        // 准备测试数据
        String sender = "Charlie";
        String receiver = "David";
        String content = "Test message";
        String type = "SYSTEM";
        String filePath = "/path/to/file.txt";
        
        // 使用 setter 方法设置属性
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        message.setType(type);
        message.setFilePath(filePath);
        
        // 验证 getter 方法返回设置的值
        assertEquals(sender, message.getSender());
        assertEquals(receiver, message.getReceiver());
        assertEquals(content, message.getContent());
        assertEquals(type, message.getType());
        assertEquals(filePath, message.getFilePath());
    }
    
    /**
     * 测试 toString 方法 - 文本消息
     */
    @Test
    public void testToStringTextMessage() {
        // 创建文本消息
        Message message = new Message("Alice", "Bob", "Hello!");
        
        // 预期的 toString 输出格式
        String expected = "Alice: Hello!";
        
        // 验证 toString 方法的输出
        assertEquals(expected, message.toString());
    }
    
    /**
     * 测试 toString 方法 - 文件消息
     */
    @Test
    public void testToStringFileMessage() {
        // 创建文件消息
        Message message = new Message("Alice", "Bob", "image.jpg", "/path/to/image.jpg");
        
        // 预期的 toString 输出格式
        String expected = "Alice: [文件] image.jpg";
        
        // 验证 toString 方法的输出
        assertEquals(expected, message.toString());
    }
    
    /**
     * 测试 toString 方法 - 系统消息
     */
    @Test
    public void testToStringSystemMessage() {
        // 创建系统消息
        Message message = new Message();
        message.setSender("System");
        message.setContent("用户已连接");
        message.setType("SYSTEM");
        
        // 预期的 toString 输出格式
        String expected = "[系统消息] 用户已连接";
        
        // 验证 toString 方法的输出
        assertEquals(expected, message.toString());
    }
} 