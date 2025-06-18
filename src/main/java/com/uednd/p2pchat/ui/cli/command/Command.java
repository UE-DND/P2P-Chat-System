package com.uednd.p2pchat.ui.cli.command;

import java.util.Scanner;

import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.service.FileTransferService;
import com.uednd.p2pchat.service.MessageService;

/**
 * 命令接口
 * <p>
 * 所有聊天命令都实现此接口
 * 
 * @version 1.0.0
 * @since 2025-06-16
 */
public interface Command {
    
    /**
     * 获取命令名称（不包含"/"前缀）
     * @return 命令名称
     */
    String getName();
    
    /**
     * 获取命令描述，用于帮助菜单显示
     * @return 命令描述
     */
    String getDescription();
    
    /**
     * 执行命令
     * @param scanner 用户输入扫描器，用于命令可能需要的额外输入
     * @param localUser 本地用户
     * @param oppositeUser 对方用户  
     * @param messageService 消息服务
     * @param fileTransferService 文件传输服务
     * @param chatRepository 聊天仓库
     * @return 如果继续聊天则返回true，否则返回false
     */
    boolean execute(
        /**
         * 在 FileCommand 中用于读取用户输入的文件路径
         * 在 ClearCommand 中通过 InputHandler.getConfirmation() 方法使用，获取用户确认操作
         */
        Scanner scanner,

        /**
         * 在 HistoryCommand 中用于判断消息发送者是否为本地用户，以不同颜色显示消息
         */
        User localUser,

        /**
         * 在 ClearCommand 中用于显示 "此操作将清空与[对方用户名]的所有聊天记录" 的提示信息
         * 在 ClearCommand 中用于在清屏后显示 "与[对方用户名]对话 "的分隔符
         */
        User oppositeUser,

        /**
         * 在 ExitCommand 中用于关闭消息服务和清理资源
         * 在 ClearCommand 中调用 clearChatHistory() 方法清空聊天记录
         * 在 HistoryCommand 中调用 getChatHistory() 获取历史消息
         */
        MessageService messageService, 

        /**
         * 在 FileCommand 中调用 sendFile() 方法发送文件
         */
        FileTransferService fileTransferService,

        /**
         * 间接通过 MessageService 和 FileTransferService 使用，
         * 这两个服务内部会使用 ChatRepository
         */
        ChatRepository chatRepository
    );
} 