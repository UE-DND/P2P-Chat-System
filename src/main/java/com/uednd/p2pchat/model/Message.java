package com.uednd.p2pchat.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 消息类，表示聊天消息
 * 
 * @version 1.0.0
 * @since 2025-06-06
 */
@Setter
@Getter
public class Message {
    // 发送者
    private String sender;

    // 接收者
    private String receiver;

    // 消息内容
    private String content;

    // 消息类型（文本、文件、系统消息）
    private String type;

    // 文件路径（如果是文件消息）
    private String filePath;
    
    /**
     * 空构造函数
     */
    public Message() {}

    /**
     * 文本消息构造函数
     * @param sender 发送者
     * @param receiver 接收者
     * @param content 消息内容
     */
    public Message(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = "TEXT";
    }
    
    /**
     * 文件消息构造函数
     * @param sender 发送者
     * @param receiver 接收者
     * @param content 消息内容
     * @param filePath 文件路径
     */
    public Message(String sender, String receiver, String content, String filePath) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.type = "FILE";
        this.filePath = filePath;
    }

    /**
     * 重写toString方法，用于显示消息信息
     * @return 消息信息字符串
     */
    @Override
    public String toString() {
        if ("FILE".equals(type)) {
            return sender + ": [文件] " + content;
        } else if ("SYSTEM".equals(type)) {
            return "[系统消息] " + content;
        } else {
            return sender + ": " + content;
        }
    }
} 