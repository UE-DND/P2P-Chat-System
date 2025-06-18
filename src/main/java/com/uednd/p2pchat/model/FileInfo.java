package com.uednd.p2pchat.model;

import lombok.Getter;
import lombok.Setter;

import java.io.*;

/**
 * 文件信息类
 * <p>
 * 表示文件传输信息
 * 
 * @version 1.0.0
 * @since 2025-06-06
 */
@Setter
@Getter
public class FileInfo implements Serializable {
    
    // 文件名
    private String fileName;

    // 文件大小（字节）
    private long fileSize;

    // 文件二进制数据
    private byte[] fileData;

    // 发送者
    private String sender;

    // 接收者
    private String receiver;
    
    /**
     * 构造函数
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param fileData 文件字节数据
     * @param sender 发送者
     * @param receiver 接收者
     */
    public FileInfo(String fileName, long fileSize, byte[] fileData, String sender, String receiver) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileData = fileData;
        this.sender = sender;
        this.receiver = receiver;
    }

    /**
     * 重写toString方法，用于显示文件信息
     * @return 文件信息字符串
     */
    @Override
    public String toString() {
        return "文件: " + fileName + " (大小: " + formatFileSize(fileSize) + ")";
    }
    
    /**
     * 格式化文件大小，转换为可读的形式
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小字符串
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
} 