package com.uednd.p2pchat.util;

import java.io.File;

/**
 * 目录创建工具类
 * <p>
 * 用于创建目录，如果它不存在
 * 
 * @version 1.0.0
 * @since 2025-06-05
 */
public class DirectoryUtils {

    /**
     * 创建目录，如果它不存在
     * 
     * @param path 目录路径
     * @return 如果目录已存在或创建成功则返回 true
     */
    public static boolean createDirectoryIfNotExists(String path) {
        File directory = new File(path);
        if (directory.exists())
            return directory.isDirectory();  // 检查是否为目录，是则返回true
        else return directory.mkdirs();  // 创建多级目录，成功则返回true
    }
} 