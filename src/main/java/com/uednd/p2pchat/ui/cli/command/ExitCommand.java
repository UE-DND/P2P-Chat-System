package com.uednd.p2pchat.ui.cli.command;

import java.util.Scanner;

import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.network.NetworkManager;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.service.FileTransferService;
import com.uednd.p2pchat.service.MessageService;
import com.uednd.p2pchat.util.ANSIcolor;

/**
 * 退出命令实现
 * <p>
 * 结束当前聊天
 * 
 * @version 1.0.0
 * @since 2025-06-17
 */
public class ExitCommand implements Command {

    private final NetworkManager networkManager;
    private static final String CHAT_CLOSE_NOTIFY = "CHAT_CLOSE_NOTIFY";
    
    /**
     * 构造函数
     * @param networkManager 网络管理器，用于发送退出通知
     */
    public ExitCommand(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public String getName() {
        return "exit";
    }

    @Override
    public String getDescription() {
        return "退出当前会话";
    }

    @Override
    public boolean execute(
        Scanner scanner, 
        User localUser, 
        User oppositeUser, 
        MessageService messageService, 
        FileTransferService fileTransferService, 
        ChatRepository chatRepository
    ) {
        System.out.println(ANSIcolor.CYAN + "正在退出聊天..." + ANSIcolor.RESET);
        
        // 关闭聊天会话并清理资源
        shutdownChat(messageService, true);
        
        return false; // 返回false停止聊天循环
    }
    
    /**
     * 关闭聊天会话并清理资源
     * @param messageService 消息服务
     * @param notify_opposite_user 如果为 true，则向对方发送关闭通知
     */
    private synchronized void shutdownChat(MessageService messageService, boolean notify_opposite_user) {
        // 如果消息服务未启动，则直接返回
        if (messageService == null || !messageService.isRunning()) {
            return;
        }

        // 如果需要提醒
        if (notify_opposite_user && networkManager.isConnected()) {
            networkManager.sendTextMessage(CHAT_CLOSE_NOTIFY);
        }

        // 停止消息服务
        messageService.stop();
        
        // 关闭网络连接
        networkManager.closeConnection();
    }
} 