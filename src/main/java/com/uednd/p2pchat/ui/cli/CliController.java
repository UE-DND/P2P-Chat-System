package com.uednd.p2pchat.ui.cli;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.network.NetworkManager;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.ui.cli.util.MenuDisplay;
import com.uednd.p2pchat.util.ANSIcolor;
import com.uednd.p2pchat.util.NetworkUtils;


/**
 * 命令行界面类
 * <p>
 * 负责与用户交互，包括显示主菜单、接收模式、发送模式等
 * 
 * @version 1.0.0
 * @since 2025-06-07
 */
public class CliController {
    private final Scanner scanner;
    
    // 数据库管理器
    private ChatRepository chat_history;

    // 网络管理器
    private NetworkManager networkManager;
    
    // 生成本地用户
    private User localUser;
    
    /**
     * 构造函数
     * @param sql_path 数据库文件夹名称
     * @param download_path 下载文件夹名称
     */
    public CliController(String sql_path, String download_path) {
        // System.out.println("DEBUG: [CliController::构造函数] - 初始化命令行界面控制器");
        scanner = new Scanner(System.in);

        try {
            // 获取用户自定义端口
            // System.out.println("DEBUG: [CliController::构造函数] - 请求用户输入端口");
            int user_defined_port = getPortFromUser();
            // System.out.println("DEBUG: [CliController::构造函数] - 用户选择端口: " + user_defined_port);

            // 清屏
            MenuDisplay.clearScreen();

            // 创建网络对象，传入用户自定义端口
            // System.out.println("DEBUG: [CliController::构造函数] - 初始化网络管理器");
            networkManager = new NetworkManager(user_defined_port);
            
            // 传入数据库名称，创建并初始化数据库
            // System.out.println("DEBUG: [CliController::构造函数] - 初始化数据库: " + sql_path);
            chat_history = new ChatRepository(sql_path);
            chat_history.initDatabase();

            // 获取本地主机名，传入用户自定义端口供默认名称使用
            String hostname = getLocalHostname(user_defined_port);
            // System.out.println("DEBUG: [CliController::构造函数] - 获取到主机名: " + hostname);

            // 设置本地用户信息，传入本地主机名
            // System.out.println("DEBUG: [CliController::构造函数] - 设置本地用户信息");
            setupLocalUser(hostname);

            // 打印IP和端口信息
            System.out.println(ANSIcolor.GREEN + "你好, " + ANSIcolor.BOLD + hostname + ANSIcolor.RESET + ANSIcolor.GREEN + "!" + ANSIcolor.RESET);
            System.out.println(ANSIcolor.GREEN + "你的IP地址为: " + ANSIcolor.BOLD + localUser.getIpAddress() + ANSIcolor.RESET + ANSIcolor.GREEN + ", 端口号: " + ANSIcolor.BOLD + localUser.getPort() + ANSIcolor.RESET);
            
            // 显示主菜单
            // System.out.println("DEBUG: [CliController::构造函数] - 显示主菜单");
            showMainMenu(download_path);
            
        } catch (Exception e) {
            System.out.println(ANSIcolor.RED + "初始化失败: " + e.getMessage() + ANSIcolor.RESET);
            System.err.println("详细错误信息: " + e);
            // System.out.println("DEBUG: [CliController::构造函数] - 初始化异常: " + e.getMessage());
        } finally {
            // System.out.println("DEBUG: [CliController::构造函数] - 关闭资源");
            closeResources();  // showMainMenu中选择退出程序后，关闭所有资源
        }
    }
    
    /**
     * 获取本机主机名
     * @param user_defined_port 用户自定义端口
     * @return 本机主机名
     */
    private String getLocalHostname(int user_defined_port) {
        // System.out.println("DEBUG: [CliController::getLocalHostname] - 获取本机主机名");
        String hostname = NetworkUtils.getHostname();
        if (hostname == null) {
            hostname = "User-" + user_defined_port;
            System.out.println(ANSIcolor.YELLOW + "无法获取主机名，使用默认名称: " + hostname + ANSIcolor.RESET);
            // System.out.println("DEBUG: [CliController::getLocalHostname] - 使用默认主机名: " + hostname);
        } else {
            // System.out.println("DEBUG: [CliController::getLocalHostname] - 获取到系统主机名: " + hostname);
        }
        return hostname;
    }
    
    /**
     * 获取用户自定义端口
     * @return 用户选择的有效端口号或默认端口号
     */
    private int getPortFromUser() {
        // System.out.println("DEBUG: [CliController::getPortFromUser] - 请求用户输入端口");
        final int DEFAULT_PORT = 11451;
        int port;
        while (true) {
            System.out.print(ANSIcolor.YELLOW + "请输入自定义端口号 (回车使用默认端口 " + DEFAULT_PORT + "): " + ANSIcolor.RESET);
            String portStr = scanner.nextLine().trim();

            if (portStr.isEmpty()) {
                port = DEFAULT_PORT;  // 用户输入为空，则返回默认端口号
                // System.out.println("DEBUG: [CliController::getPortFromUser] - 使用默认端口: " + DEFAULT_PORT);
            } else {
                try {
                    port = Integer.parseInt(portStr);
                    // System.out.println("DEBUG: [CliController::getPortFromUser] - 解析用户输入端口: " + port);
                } catch (NumberFormatException e) {
                    System.out.println(ANSIcolor.RED + "无效的输入。请输入一个数字。" + ANSIcolor.RESET);
                    // System.out.println("DEBUG: [CliController::getPortFromUser] - 端口解析失败: " + e.getMessage());
                    continue;
                }
            }

            // 使用网络基础工具检查端口状态，传入用户自定义端口号或默认端口号
            NetworkUtils.PortStatus status = NetworkUtils.checkPort(port);
            // System.out.println("DEBUG: [CliController::getPortFromUser] - 端口状态检查结果: " + status + " 端口: " + port);
            switch (status) {
                case AVAILABLE:
                    return port; // 端口可用，返回端口号
                case IN_USE:
                    System.out.println(ANSIcolor.RED + "端口 " + port + " 已被占用。请选择其他端口。" + ANSIcolor.RESET);
                    break;
                case INVALID_RANGE:
                    System.out.println(ANSIcolor.RED + "无效的端口号。请输入 1-65535 之间的数字。" + ANSIcolor.RESET);
                    break;
            }
        }
    }
    
    /**
     * 设置本地用户信息
     * @param hostname 本地用户名（主机名）
     * @throws SQLException 如果保存用户信息失败则抛出异常
     */
    private void setupLocalUser(String hostname) throws SQLException {
        // System.out.println("DEBUG: [CliController::setupLocalUser] - 设置本地用户信息: " + hostname);
        // 获取本地IP地址
        String localIp = NetworkUtils.getLocalIpAddress();
        // System.out.println("DEBUG: [CliController::setupLocalUser] - 获取到本地IP: " + localIp);
        
        // 创建本地用户对象，传入主机名、本地IP地址、自定义端口
        localUser = new User(hostname, localIp, networkManager.getPort());
        // System.out.println("DEBUG: [CliController::setupLocalUser] - 创建本地用户对象: " + localUser);
        
        // 保存用户信息到数据库，这里传入本地用户
        chat_history.saveUser(localUser);
        // System.out.println("DEBUG: [CliController::setupLocalUser] - 本地用户信息已保存到数据库");
    }
    
    /**
     * 显示主菜单
     * @param download_path 文件下载路径
     * @throws IOException 如果网络操作失败则抛出异常
     */
    private void showMainMenu(String download_path) throws IOException {
        // System.out.println("DEBUG: [CliController::showMainMenu] - 显示主菜单");
        boolean running = true;
        
        while (running) {
            // 不再在此处清屏，因为启动时已经清过一次
            // MenuDisplay.clearScreen(); 
            MenuDisplay.printSeparator("P2P Chat");
            System.out.println();
            System.out.println("  " + ANSIcolor.YELLOW + "[" + ANSIcolor.GREEN + "1" + ANSIcolor.YELLOW + "]" + ANSIcolor.WHITE + " 📡 发送模式" + ANSIcolor.RESET);
            System.out.println("  " + ANSIcolor.YELLOW + "[" + ANSIcolor.GREEN + "2" + ANSIcolor.YELLOW + "]" + ANSIcolor.WHITE + " 📥 接收模式" + ANSIcolor.RESET);
            System.out.println("  " + ANSIcolor.YELLOW + "[" + ANSIcolor.RED   + "3" + ANSIcolor.YELLOW + "]" + ANSIcolor.WHITE + " 🚪 退出程序" + ANSIcolor.RESET);
            System.out.println();
            MenuDisplay.printSeparator(null);
            System.out.println();
            System.out.print(ANSIcolor.YELLOW + " 请选择操作 " + ANSIcolor.GREEN + ANSIcolor.BOLD + "(1-3)" + ANSIcolor.RESET + ANSIcolor.YELLOW + ": " + ANSIcolor.RESET);
            
            String choice = scanner.nextLine().trim();
            // System.out.println("DEBUG: [CliController::showMainMenu] - 用户选择: " + choice);
            
            switch (choice) {
                case "1":
                    // System.out.println("DEBUG: [CliController::showMainMenu] - 进入发送模式");
                    MenuDisplay.clearScreen();
                    sendMode(download_path);
                    break;
                case "2":
                    // System.out.println("DEBUG: [CliController::showMainMenu] - 进入接收模式");
                    MenuDisplay.clearScreen();
                    receiveMode(download_path);
                    break;
                case "3":
                    // System.out.println("DEBUG: [CliController::showMainMenu] - 退出程序");
                    running = false;
                    System.out.println(ANSIcolor.CYAN + "正在退出..." + ANSIcolor.RESET);
                    break;
                default:
                    System.out.println(ANSIcolor.RED + "无效的选择，请重试。" + ANSIcolor.RESET);
                    // System.out.println("DEBUG: [CliController::showMainMenu] - 无效选择: " + choice);
                    // 清屏前短暂显示错误消息，再返回主菜单
                    try { 
                        Thread.sleep(2000);  // 线程休眠2秒
                    } catch (InterruptedException e) { 
                        Thread.currentThread().interrupt();  // 使当前线程中断，继续执行下一步操作
                    }
                    MenuDisplay.clearScreen();
            }
        }
        // System.out.println("DEBUG: [CliController::showMainMenu] - 退出主菜单循环");
    }
    
    /**
     * 接收模式菜单
     * @param downloadPath 文件下载路径
     * @throws IOException 如果网络操作失败则抛出异常
     */
    private void receiveMode(String downloadPath) throws IOException {
        // System.out.println("DEBUG: [CliController::receiveMode] - 显示接收模式菜单");
        MenuDisplay.printSeparator("📥 接收模式");
        System.out.println("  " + ANSIcolor.YELLOW + "[" + ANSIcolor.GREEN + "1" + ANSIcolor.YELLOW + "]" + ANSIcolor.WHITE + " 💬 等待连接" + ANSIcolor.RESET);
        System.out.println("  " + ANSIcolor.YELLOW + "[" + ANSIcolor.GREEN + "2" + ANSIcolor.YELLOW + "]" + ANSIcolor.WHITE + " ↩️  返回主菜单" + ANSIcolor.RESET);
        System.out.println();
        System.out.print(ANSIcolor.YELLOW + " 请选择操作 " + ANSIcolor.GREEN + ANSIcolor.BOLD + "[1-2]" + ANSIcolor.RESET + ANSIcolor.YELLOW + ": " + ANSIcolor.RESET);
        
        String choice = scanner.nextLine().trim();
        // System.out.println("DEBUG: [CliController::receiveMode] - 用户选择: " + choice);
        
        switch (choice) {
            case "1":
                // System.out.println("DEBUG: [CliController::receiveMode] - 进入等待连接模式");
                receiveMessageMode(downloadPath);
                break;
            case "2":
                // System.out.println("DEBUG: [CliController::receiveMode] - 返回主菜单");
                MenuDisplay.clearScreen();
                return;
            default:
                System.out.println(ANSIcolor.RED + "无效的选择，请重试。" + ANSIcolor.RESET);
                // System.out.println("DEBUG: [CliController::receiveMode] - 无效选择: " + choice);
        }
    }
    
    /**
     * 发送模式
     * @param download_path 文件下载路径
     */
    private void sendMode(String download_path) {
        // System.out.println("DEBUG: [CliController::sendMode] - 进入发送模式");
        MenuDisplay.printSeparator("📡 发送模式");

        // 创建 ConnectionHandler 对象，传入输入流、网络管理器（包括所有socket对象）、本地用户
        // System.out.println("DEBUG: [CliController::sendMode] - 创建连接处理器");
        ConnectionHandler connectionHandler = new ConnectionHandler(scanner, networkManager, localUser);
        // 建立连接后握手，返回对方用户对象
        // System.out.println("DEBUG: [CliController::sendMode] - 尝试建立连接");
        User opposite_User = connectionHandler.establishConnection();

        // 连接或握手失败（返回null），返回主菜单
        if (opposite_User == null) {
            // System.out.println("DEBUG: [CliController::sendMode] - 连接失败，返回主菜单");
            MenuDisplay.clearScreen();
            return;
        }

        // 创建并聊天会话实例，注册命令
        // System.out.println("DEBUG: [CliController::sendMode] - 连接成功，创建聊天会话");
        ChatSession chatSession = new ChatSession(scanner, networkManager, chat_history, localUser, opposite_User, download_path);
        
        // 启动会话实例
        // System.out.println("DEBUG: [CliController::sendMode] - 启动聊天会话");
        chatSession.start();
        // System.out.println("DEBUG: [CliController::sendMode] - 聊天会话结束，返回主菜单");
    }
    
    /**
     * 接收消息模式
     * @param downloadPath 文件下载路径
     * @throws IOException 如果网络操作失败则抛出异常
     */
    private void receiveMessageMode(String downloadPath) throws IOException {
        // System.out.println("DEBUG: [CliController::receiveMessageMode] - 进入接收消息模式");
        System.out.println(ANSIcolor.YELLOW + "⏳ 正在等待对方连接，请稍候 (超时时间: 30秒)..." + ANSIcolor.RESET);
        
        try {
            // 在当前端口启动服务器，设置超时时间为30秒
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 启动服务器，监听端口: " + networkManager.getPort());
            networkManager.startServer(30000);
            
            // 等待客户端连接，如果超时则抛出异常
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 等待客户端连接...");
            networkManager.waitForConnection();
            
            // 客户端已连接，接收握手信息（连接方首先发送用户名）
            String opposite_Username = networkManager.receiveHandshakeMessage();
            
            // 检查对方用户名是否有效
            if (opposite_Username == null || opposite_Username.isEmpty()) {
                // System.out.println("DEBUG: [CliController::receiveMessageMode] - 握手失败，对方用户名无效");
                System.out.println(ANSIcolor.RED + "握手失败：未能获取到对方用户名。" + ANSIcolor.RESET);
                networkManager.closeConnection();
                return;
            }
            
            // 向客户端发送本地用户名
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 发送本地用户名: " + localUser.getUsername());
            networkManager.sendHandshakeMessage(localUser.getUsername());
            
            // 创建对方用户对象
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 握手成功，创建对方用户对象");
            String opposite_UserIP = networkManager.getConnectedClientIp();
            int opposite_UserPort = networkManager.getConnectedClientPort();
            User opposite_User = new User(opposite_Username, opposite_UserIP, opposite_UserPort);
            
            // 显示连接成功信息
            MenuDisplay.clearScreen();
            System.out.println(ANSIcolor.GREEN + opposite_Username + " 已连接！" + ANSIcolor.RESET);
            
            // 创建聊天会话
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 创建聊天会话");
            ChatSession chatSession = new ChatSession(scanner, networkManager, chat_history, localUser, opposite_User, downloadPath);
            
            // 启动聊天会话
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 启动聊天会话");
            chatSession.start();
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 聊天会话结束，返回主菜单");
            
        } catch (IOException e) {
            if (e.getMessage().contains("timed out")) {
                System.out.println(ANSIcolor.RED + "等待连接超时。" + ANSIcolor.RESET);
                // System.out.println("DEBUG: [CliController::receiveMessageMode] - 连接超时: " + e.getMessage());
            } else {
                System.out.println(ANSIcolor.RED + "连接错误: " + e.getMessage() + ANSIcolor.RESET);
                // System.out.println("DEBUG: [CliController::receiveMessageMode] - 连接错误: " + e.getMessage());
            }
        } finally {
            // 清理资源，确保端口在失败后被释放
            networkManager.closeConnection();
            // System.out.println("DEBUG: [CliController::receiveMessageMode] - 连接已关闭");
        }
    }

    /**
     * 关闭资源
     */
    private void closeResources() {
        // System.out.println("DEBUG: [CliController::closeResources] - 开始关闭资源");
        try {
            if (networkManager != null) {
                networkManager.shutdown();
                // System.out.println("DEBUG: [CliController::closeResources] - 网络管理器已关闭");
            }
            
            if (chat_history != null) {
                chat_history.closeConnection();
                // System.out.println("DEBUG: [CliController::closeResources] - 数据库连接已关闭");
            }
            
            if (scanner != null) {
                scanner.close();
                // System.out.println("DEBUG: [CliController::closeResources] - 输入扫描器已关闭");
            }
        } catch (Exception e) {
            System.err.println("关闭资源时出错: " + e.getMessage());
            // System.out.println("DEBUG: [CliController::closeResources] - 关闭资源异常: " + e.getMessage());
        }
        // System.out.println("DEBUG: [CliController::closeResources] - 所有资源已关闭");
    }
} 