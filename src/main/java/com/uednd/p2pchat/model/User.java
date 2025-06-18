package com.uednd.p2pchat.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户类，表示聊天用户
 * 
 * @version 1.0.0
 * @since 2025-06-06
 */
@Setter
@Getter
public class User {
    // 用户名
    private String username;

    // IP地址
    private String ipAddress;

    // 端口号
    private int port;

    /**
     * 带参数的构造函数
     * 
     * @param username 用户名
     * @param ipAddress IP地址
     * @param port 端口号
     */
    public User(String username, String ipAddress, int port) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    /**
     * 重写toString方法，用于显示用户信息
     * 
     * @return 用户信息字符串
     */
    @Override
    public String toString() {
        return username + " (" + ipAddress + ":" + port + ")";
    }
} 