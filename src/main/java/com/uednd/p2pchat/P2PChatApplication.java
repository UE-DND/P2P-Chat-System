package com.uednd.p2pchat;

import com.uednd.p2pchat.ui.cli.CliController;
import com.uednd.p2pchat.util.ANSIcolor;
import com.uednd.p2pchat.util.DirectoryUtils;

/**
 * 应用程序主类
 * <p>
 * 负责初始化应用程序，包括创建下载目录和启动命令行界面
 * 
 * @version 1.0.0
 * @since 2025-06-07
 */
public class P2PChatApplication {

    private static final String SQL_PATH = "chat_history.db";
    private static final String DOWNLOAD_PATH = "downloads";

    public static void main(String[] args) {
        // System.out.println("DEBUG: [P2PChatApplication::main] - 程序启动");
        // System.out.println("DEBUG: [P2PChatApplication::main] - SQL路径: " + SQL_PATH + ", 下载路径: " + DOWNLOAD_PATH);

        // 创建下载目录
        if (!DirectoryUtils.createDirectoryIfNotExists(DOWNLOAD_PATH)) {
            System.out.println(ANSIcolor.RED + "创建下载目录失败: " + DOWNLOAD_PATH + ANSIcolor.RESET);
            // System.out.println("DEBUG: [P2PChatApplication::main] - 创建下载目录失败，程序退出");
            return;
        }
        // System.out.println("DEBUG: [P2PChatApplication::main] - 下载目录创建成功或已存在");

        // 启动命令行界面
        // System.out.println("DEBUG: [P2PChatApplication::main] - 初始化命令行界面控制器");
        new CliController(SQL_PATH, DOWNLOAD_PATH);
        // System.out.println("DEBUG: [P2PChatApplication::main] - 程序执行完毕，退出");
    }
} 