package com.uednd.p2pchat.ui.cli;

import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.network.NetworkManager;
import com.uednd.p2pchat.ui.cli.util.InputHandler;
import com.uednd.p2pchat.util.ANSIcolor;
import java.io.IOException;
import java.util.Scanner;

/**
 * 处理发送模式连接
 * <p>
 * 负责与对方用户建立连接和握手
 * 
 * @version 1.0.0
 * @since 2025-06-17
 */
public class ConnectionHandler {
    
    // 输入输出流
    private final Scanner scanner;
    // 网络管理器
    private final NetworkManager networkManager;
    
    // 本地用户
    private final User localUser;
    
    // 最大重试次数
    private static final int MAX_RETRIES = 3;

    // 重试延迟时间
    private static final int RETRY_DELAY_SECONDS = 5;

    /**
     * 构造函数
     * @param scanner 输入流
     * @param networkManager 网络管理器
     * @param localUser 本地用户
     */
    public ConnectionHandler(Scanner scanner, NetworkManager networkManager, User localUser) {
        // System.out.println("DEBUG: [ConnectionHandler::构造函数] - 初始化连接处理器，本地用户: " + localUser.getUsername());
        this.scanner = scanner;
        this.networkManager = networkManager;
        this.localUser = localUser;
    }

    /**
     * 与对方用户建立连接
     * @return 成功连接则返回对方 User 对象, 否则返回 null
     */
    public User establishConnection() {
        // System.out.println("DEBUG: [ConnectionHandler::establishConnection] - 开始建立连接");
        try {
            // 获取用户输入
            String opposite_UserIP = InputHandler.getValidInput(scanner, "请输入对方IP地址", "IP地址不能为空");
            // System.out.println("DEBUG: [ConnectionHandler::establishConnection] - 用户输入IP: " + opposite_UserIP);
            
            int opposite_UserPort = Integer.parseInt(InputHandler.getValidInput(scanner, "请输入对方端口号", "端口号不能为空"));
            // System.out.println("DEBUG: [ConnectionHandler::establishConnection] - 用户输入端口: " + opposite_UserPort);

            // 在tryToConnectOppositeUser中自动重试三次，如果全都连接失败，则方法返回false，进入if返回主菜单
            if (!tryToConnectOppositeUser(opposite_UserIP, opposite_UserPort)) {
                // System.out.println("DEBUG: [ConnectionHandler::establishConnection] - 连接失败次数过多");
                System.out.println(ANSIcolor.RED + "连接失败次数过多，正在返回主菜单。" + ANSIcolor.RESET);
                Thread.sleep(2000);
                return null;
            }

            // 执行握手
            // System.out.println("DEBUG: [ConnectionHandler::establishConnection] - 连接成功，开始握手");
            return performHandshake(opposite_UserIP, opposite_UserPort);

        } catch (NumberFormatException e) {
            // System.out.println("DEBUG: [ConnectionHandler::establishConnection] - 端口号格式错误: " + e.getMessage());
            System.out.println(ANSIcolor.RED + "无效的端口号格式。" + ANSIcolor.RESET);
            return null;
        } catch (IOException | InterruptedException e) {
            // System.out.println("DEBUG: [ConnectionHandler::establishConnection] - 连接异常: " + e.getMessage());
            System.out.println(ANSIcolor.RED + "建立连接时出错: " + e.getMessage() + ANSIcolor.RESET);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 连接重试逻辑
     * @param opposite_UserIP 对方用户IP地址
     * @param opposite_UserPort 对方用户端口号
     * @return 是否连接成功
     * @throws InterruptedException 线程中断异常
     */
    private boolean tryToConnectOppositeUser(String opposite_UserIP, int opposite_UserPort) throws InterruptedException {
        // System.out.println("DEBUG: [ConnectionHandler::tryToConnectOppositeUser] - 开始尝试连接到 " + opposite_UserIP + ":" + opposite_UserPort);
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                // System.out.println("DEBUG: [ConnectionHandler::tryToConnectOppositeUser] - 尝试第 " + (i + 1) + " 次连接");
                System.out.println(ANSIcolor.CYAN + "正在尝试连接到 " + opposite_UserIP + ":" + opposite_UserPort + " (第 " + (i + 1) + " 次)..." + ANSIcolor.RESET);
                networkManager.connectToServer(opposite_UserIP, opposite_UserPort);
                // System.out.println("DEBUG: [ConnectionHandler::tryToConnectOppositeUser] - 连接成功");
                return true;  // 连接成功
            } catch (IOException e) {
                // System.out.println("DEBUG: [ConnectionHandler::tryToConnectOppositeUser] - 连接失败: " + e.getMessage());
                System.out.println(ANSIcolor.RED + "连接失败: " + e.getMessage() + ANSIcolor.RESET);
                if (i < MAX_RETRIES - 1) {
                    // System.out.println("DEBUG: [ConnectionHandler::tryToConnectOppositeUser] - 等待 " + RETRY_DELAY_SECONDS + " 秒后重试");
                    System.out.println(ANSIcolor.YELLOW + "将在 " + RETRY_DELAY_SECONDS + " 秒后重试... (剩余 " + (MAX_RETRIES - 1 - i) + " 次)" + ANSIcolor.RESET);
                    Thread.sleep(RETRY_DELAY_SECONDS * 1000);
                }
            }
        }
        // System.out.println("DEBUG: [ConnectionHandler::tryToConnectOppositeUser] - 已达到最大重试次数，连接失败");
        return false;
    }

    /**
     * 执行握手
     * @param opposite_UserIP 对方用户IP地址
     * @param opposite_UserPort 对方用户端口号
     * @return 成功握手则返回对方用户对象, 否则返回 null
     * @throws IOException 如果握手失败则抛出异常
     */
    private User performHandshake(String opposite_UserIP, int opposite_UserPort) throws IOException {
        // System.out.println("DEBUG: [ConnectionHandler::performHandshake] - 开始执行握手");
        // 发送自己的用户名
        // System.out.println("DEBUG: [ConnectionHandler::performHandshake] - 发送本地用户名: " + localUser.getUsername());
        networkManager.sendHandshakeMessage(localUser.getUsername());

        // 接收对方的用户名
        // System.out.println("DEBUG: [ConnectionHandler::performHandshake] - 等待接收对方用户名");
        String opposite_Username = networkManager.receiveHandshakeMessage();
        // System.out.println("DEBUG: [ConnectionHandler::performHandshake] - 接收到对方用户名: " + opposite_Username);

        // 如果对方用户名不存在或为空，则握手失败
        if (opposite_Username == null || opposite_Username.trim().isEmpty()) {
            // System.out.println("DEBUG: [ConnectionHandler::performHandshake] - 握手失败，对方用户名无效");
            System.out.println(ANSIcolor.RED + "握手失败：未能获取到对方用户名。" + ANSIcolor.RESET);
            networkManager.closeConnection();
            return null;
        }
        
        // System.out.println("DEBUG: [ConnectionHandler::performHandshake] - 握手成功");
        System.out.println(ANSIcolor.GREEN + "连接成功，对方用户名为: " + ANSIcolor.BOLD + opposite_Username + ANSIcolor.RESET);
        return new User(opposite_Username, opposite_UserIP, opposite_UserPort);  // 返回对方用户对象
    }
} 