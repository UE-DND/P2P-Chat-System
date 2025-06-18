package com.uednd.p2pchat.ui.cli;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.uednd.p2pchat.model.Message;
import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.network.NetworkManager;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.service.FileTransferService;
import com.uednd.p2pchat.service.MessageService;
import com.uednd.p2pchat.ui.cli.command.*;
import com.uednd.p2pchat.ui.cli.util.InputHandler;
import com.uednd.p2pchat.ui.cli.util.MenuDisplay;
import com.uednd.p2pchat.util.ANSIcolor;

/**
 * 聊天会话
 * <p>
 * 管理用户之间的聊天过程
 * 
 * @version 1.0.0
 * @since 2025-06-16
 */
public class ChatSession {
    
    // 退出聊天的消息标识符
    private static final String CHAT_CLOSE_NOTIFY = "CHAT_CLOSE_NOTIFY";
    
    // 用户输入扫描器
    private final Scanner scanner;
    
    // 网络管理器
    private final NetworkManager networkManager;
    
    // 数据库仓库
    private final ChatRepository sql_path;
    
    // 本地用户
    private final User localUser;
    
    // 对方用户
    private final User opposite_User;
    
    // 文件下载路径
    private final String download_path;
    
    // 消息服务
    private MessageService messageService;

    // 文件传输服务
    private FileTransferService fileTransferService;
    
    // 命令列表
    private final List<Command> commands = new ArrayList<Command>();

    /**
     * 构造方法
     * @param scanner 用于用户输入的扫描器
     * @param networkManager 网络管理器
     * @param sql_path 数据库仓库
     * @param localUser 本地用户
     * @param opposite_User 对方用户
     * @param downloadPath 文件下载路径
     */
    public ChatSession(Scanner scanner, NetworkManager networkManager, ChatRepository sql_path, User localUser, User opposite_User, String downloadPath) {
        // System.out.println("DEBUG: [ChatSession::构造函数] - 初始化聊天会话，本地用户: " + localUser.getUsername() + ", 对方用户: " + opposite_User.getUsername());
        this.scanner = scanner;
        this.networkManager = networkManager;
        this.sql_path = sql_path;
        this.localUser = localUser;
        this.opposite_User = opposite_User;
        this.download_path = downloadPath;
        
        // 注册所有命令
        initCommands();
    }

    /**
     * 初始化（注册）所有命令
     */
    private void initCommands() {
        // System.out.println("DEBUG: [ChatSession::initCommands] - 注册聊天命令");
        // 创建和添加所有命令实例
        commands.add(new ExitCommand(networkManager));  // 传入networkManager以发送退出通知
        commands.add(new FileCommand());
        commands.add(new ClearCommand());
        commands.add(new HistoryCommand());
        
        // 帮助菜单，传入命令列表本身
        commands.add(new HelpCommand(commands));
        // System.out.println("DEBUG: [ChatSession::initCommands] - 已注册 " + commands.size() + " 个命令");
    }

    /**
     * 启动并管理聊天会话
     */
    public void start() {
        // System.out.println("DEBUG: [ChatSession::start] - 启动聊天会话");
        try {
            // 初始化文件传输服务
            initializeFileTransferService();

            MenuDisplay.printSeparator("与 " + opposite_User.getUsername() + " 对话");
            System.out.println(ANSIcolor.YELLOW + " (输入 /help 查看可用命令)" + ANSIcolor.RESET);
            MenuDisplay.printSeparator(null);

            // 显示历史消息
            showChatHistory();

            // 初始化消息服务并启动后台服务（消息处理器）
            // System.out.println("DEBUG: [ChatSession::start] - 创建消息处理器");
            MessageService.MessageHandler messageHandler = createMessageHandler();
            this.messageService = new MessageService(networkManager, sql_path, localUser.getUsername(), opposite_User.getUsername(), messageHandler);
            messageService.start();
            // System.out.println("DEBUG: [ChatSession::start] - 消息服务已启动");
            
            // 运行聊天循环
            // System.out.println("DEBUG: [ChatSession::start] - 开始聊天循环");
            runChatLoop();

        } catch (Exception e) {
            System.out.println(ANSIcolor.RED + "会话过程出错: " + e.getMessage() + ANSIcolor.RESET);
            // System.out.println("DEBUG: [ChatSession::start] - 会话出错: " + e.getMessage());
        } finally {
            // 确保资源被关闭
            // System.out.println("DEBUG: [ChatSession::start] - 会话结束，关闭资源");
            shutdownChat(false);  // 对方退出聊天，不通知

            // 添加分隔符表示会话结束
            MenuDisplay.printSeparator("会话已结束");

            // 等待用户确认返回主菜单
            InputHandler.waitForEnter(scanner, "按回车键返回主菜单...");
            MenuDisplay.clearScreen();
        }
    }

    /**
     * 创建消息处理器
     * @return 一个实现了消息处理逻辑的 {@code MessageHandler} 实例
     */
    private MessageService.MessageHandler createMessageHandler() {
        // System.out.println("DEBUG: [ChatSession::createMessageHandler] - 创建消息处理器实例");
        return new MessageService.MessageHandler() {
            private boolean chatActive = true;  // 聊天是否继续进行，对方退出后会置为false

            public void handleMessage(String message) {
                // System.out.println("DEBUG: [ChatSession::MessageHandler::handleMessage] - 处理接收到的消息: " + message);
                // 如果聊天已结束，则不处理消息，直接返回start()的finally块
                if (!chatActive) return;

                // 如果收到对话关闭命令，则打印对方退出消息，并置为false，然后调用shutdownChat()
                if (message.equals(CHAT_CLOSE_NOTIFY)) {
                    // System.out.println("DEBUG: [ChatSession::MessageHandler::handleMessage] - 收到对方退出通知");
                    MenuDisplay.clearCurrentLine();
                    System.out.println(ANSIcolor.YELLOW + opposite_User.getUsername() + " 已退出聊天，连接已断开。" + ANSIcolor.RESET);
                    chatActive = false;
                    shutdownChat(false);  // 对方退出聊天，不发送通知
                } else if (FileTransferService.isFileMessage(message)) {
                    // System.out.println("DEBUG: [ChatSession::MessageHandler::handleMessage] - 收到文件传输请求");
                    handleFileReceive();
                } else {
                    // System.out.println("DEBUG: [ChatSession::MessageHandler::handleMessage] - 显示对方消息");
                    MenuDisplay.clearCurrentLine();
                    System.out.println(ANSIcolor.YELLOW + opposite_User.getUsername() + ": " + ANSIcolor.RESET + ANSIcolor.WHITE + message + ANSIcolor.RESET);
                    System.out.print(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET);
                }
            }

            /**
             * 处理错误消息
             * @param errorMessage 错误消息
             */
            public void handleError(String errorMessage) {
                // System.out.println("DEBUG: [ChatSession::MessageHandler::handleError] - 处理错误: " + errorMessage);
                System.out.println(ANSIcolor.RED + "\n错误: " + errorMessage + ANSIcolor.RESET);
            }
        };
    }
    
    /**
     * 运行主聊天循环
     */
    private void runChatLoop() {
        // System.out.println("DEBUG: [ChatSession::runChatLoop] - 进入主聊天循环");
        boolean chatting = true;  // 聊天是否继续进行，对方退出后会置为false
        System.out.print(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET);
        
        while (chatting) {
            // 如果消息服务未启动（例如对方网络断开），退出循环，最终执行start()的finally块【关闭聊天资源】
            if (messageService == null || !messageService.isRunning()) {
                // System.out.println("DEBUG: [ChatSession::runChatLoop] - 消息服务未运行，退出聊天循环");
                chatting = false;
                System.out.println();
                continue;
            }

            try {
                // 只有当确认有输入数据可用时，才调用scanner.nextLine()读取一行文本
                // 避免nextLine阻塞程序执行（在没有输入时一直等待）
                if (System.in.available() > 0) {
                    // System.out.println("DEBUG: [ChatSession::runChatLoop] - 检测到用户输入");
                    // 如果消息以/开头，则处理相应命令
                    String message = scanner.nextLine();
                    if (message.startsWith("/")) {
                        // System.out.println("DEBUG: [ChatSession::runChatLoop] - 处理命令: " + message);
                        chatting = handleCommand(message);
                    } else if (!message.trim().isEmpty()) {
                        // System.out.println("DEBUG: [ChatSession::runChatLoop] - 发送消息: " + message);
                        // 如果不是命令，则发送消息
                        try {
                            messageService.sendTextMessage(message);
                        } catch (IOException e) {
                            System.out.println(ANSIcolor.RED + "发送消息失败: " + e.getMessage() + ANSIcolor.RESET);
                            // System.out.println("DEBUG: [ChatSession::runChatLoop] - 发送消息失败: " + e.getMessage());
                            chatting = false;
                        } catch (SQLException e) {
                            System.out.println(ANSIcolor.RED + "保存消息记录失败: " + e.getMessage() + ANSIcolor.RESET);
                            // System.out.println("DEBUG: [ChatSession::runChatLoop] - 保存消息记录失败: " + e.getMessage());
                        }
                        System.out.print(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET);
                    } else {
                        // 用户发送了空消息，重新显示提示符
                        System.out.print(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET);
                    }
                } else {
                    // 没有用户输入，短暂休眠以避免CPU空转
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // System.out.println("DEBUG: [ChatSession::runChatLoop] - 聊天循环被中断: " + e.getMessage());
                chatting = false;
            } catch (IOException e) {
                System.out.println(ANSIcolor.RED + "读取用户输入失败: " + e.getMessage() + ANSIcolor.RESET);
                // System.out.println("DEBUG: [ChatSession::runChatLoop] - 读取用户输入失败: " + e.getMessage());
                chatting = false;
            }
        }
        // System.out.println("DEBUG: [ChatSession::runChatLoop] - 退出聊天循环");
    }
    
    /**
     * 初始化文件传输服务
     */
    private void initializeFileTransferService() {
        // System.out.println("DEBUG: [ChatSession::initializeFileTransferService] - 初始化文件传输服务");
        fileTransferService = new FileTransferService(
                networkManager,
                sql_path,
                localUser.getUsername(),
                opposite_User.getUsername(),
                download_path
        );
    }
    
    /**
     * 显示聊天历史记录
     */
    private void showChatHistory() {
        // System.out.println("DEBUG: [ChatSession::showChatHistory] - 获取并显示历史消息");
        try {
            List<Message> history = sql_path.getChatHistory(localUser.getUsername(), opposite_User.getUsername());
            if (!history.isEmpty()) {
                System.out.println(ANSIcolor.CYAN + "=== 历史消息 ===" + ANSIcolor.RESET);
                for (Message message : history) {
                    if (message.getSender().equals(localUser.getUsername())) {
                        System.out.println(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET + ANSIcolor.WHITE + message.getContent() + ANSIcolor.RESET);
                    } else {
                        System.out.println(ANSIcolor.YELLOW + opposite_User.getUsername() + ": " + ANSIcolor.RESET + ANSIcolor.WHITE + message.getContent() + ANSIcolor.RESET);
                    }
                }
                System.out.println(ANSIcolor.CYAN + "=== 结束 ===" + ANSIcolor.RESET);
                System.out.println();
            }
        } catch (SQLException e) {
            System.out.println(ANSIcolor.RED + "获取历史消息失败: " + e.getMessage() + ANSIcolor.RESET);
            // System.out.println("DEBUG: [ChatSession::showChatHistory] - 获取历史消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 处理用户命令
     * @param commandStr 命令字符串
     * @return 如果应该继续聊天则返回true，否则返回false
     */
    private boolean handleCommand(String commandStr) {
        // System.out.println("DEBUG: [ChatSession::handleCommand] - 处理命令: " + commandStr);
        String cmdName = commandStr.split(" ")[0].substring(1).toLowerCase();  // 去掉/前缀
        
        for (Command command : commands) {
            if (command.getName().equalsIgnoreCase(cmdName)) {
                // System.out.println("DEBUG: [ChatSession::handleCommand] - 找到命令处理器: " + command.getName());
                boolean continueChat = command.execute(scanner, localUser, opposite_User, messageService, fileTransferService, sql_path);
                // if (!continueChat) {
                //     System.out.println("DEBUG: [ChatSession::handleCommand] - 命令指示退出聊天");
                // }
                return continueChat;
            }
        }
        
        System.out.println(ANSIcolor.RED + "未知命令: " + cmdName + ANSIcolor.RESET);
        System.out.print(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET);
        // System.out.println("DEBUG: [ChatSession::handleCommand] - 未知命令: " + cmdName);
        return true;
    }
    
    /**
     * 处理接收文件
     */
    private void handleFileReceive() {
        // System.out.println("DEBUG: [ChatSession::handleFileReceive] - 处理文件接收");
        try {
            String filePath = fileTransferService.receiveFile();
            System.out.println(ANSIcolor.GREEN + "文件已保存到: " + filePath + ANSIcolor.RESET);
            System.out.print(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET);
            // System.out.println("DEBUG: [ChatSession::handleFileReceive] - 文件接收成功: " + filePath);
        } catch (Exception e) {
            System.out.println(ANSIcolor.RED + "接收文件失败: " + e.getMessage() + ANSIcolor.RESET);
            System.out.print(ANSIcolor.GREEN + "你: " + ANSIcolor.RESET);
            // System.out.println("DEBUG: [ChatSession::handleFileReceive] - 文件接收失败: " + e.getMessage());
        }
    }
    
    /**
     * 关闭聊天会话
     * @param notify_opposite_user 是否通知对方用户
     */
    public synchronized void shutdownChat(boolean notify_opposite_user) {
        // System.out.println("DEBUG: [ChatSession::shutdownChat] - 关闭聊天会话, 通知对方: " + notify_opposite_user);
        if (notify_opposite_user && networkManager.isConnected()) {
            try {
                // 发送关闭通知
                networkManager.sendTextMessage(CHAT_CLOSE_NOTIFY);
                // System.out.println("DEBUG: [ChatSession::shutdownChat] - 已发送聊天关闭通知");
            } catch (Exception e) {
                System.out.println(ANSIcolor.RED + "发送关闭通知失败: " + e.getMessage() + ANSIcolor.RESET);
                // System.out.println("DEBUG: [ChatSession::shutdownChat] - 发送关闭通知失败: " + e.getMessage());
            }
        }
        
        if (messageService != null && messageService.isRunning()) {
            messageService.stop();
            messageService = null;
            // System.out.println("DEBUG: [ChatSession::shutdownChat] - 消息服务已关闭");
        }
        
        fileTransferService = null;
        // System.out.println("DEBUG: [ChatSession::shutdownChat] - 文件传输服务已清理");
    }
} 