package com.uednd.p2pchat.ui.cli.command;

import java.util.List;
import java.util.Scanner;

import com.uednd.p2pchat.model.Message;
import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.service.FileTransferService;
import com.uednd.p2pchat.service.MessageService;
import com.uednd.p2pchat.util.ANSIcolor;

/**
 * 历史命令实现
 * <p>
 * 用于显示聊天历史
 * 
 * @version 1.0.0
 * @since 2025-06-17
 */
public class HistoryCommand implements Command {
    
    @Override
    public String getName() {
        return "history";
    }
    
    @Override
    public String getDescription() {
        return "显示历史消息";
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
        try {
            List<Message> history = messageService.getChatHistory();
            
            if (!history.isEmpty()) {
                // 显示历史消息
                for (Message message : history) {
                    if (message.getSender().equals(localUser.getUsername())) {
                        System.out.println(ANSIcolor.GREEN + "你: " + ANSIcolor.WHITE + message.getContent() + ANSIcolor.RESET);
                    } else {
                        System.out.println(ANSIcolor.YELLOW + message.getSender() + ": " + ANSIcolor.WHITE + message.getContent() + ANSIcolor.RESET);
                    }
                }
            } else {
                System.out.println(ANSIcolor.CYAN + "暂无对话记录。" + ANSIcolor.RESET);
            }
        } catch (Exception e) {
            System.out.println(ANSIcolor.RED + "加载对话历史失败: " + e.getMessage() + ANSIcolor.RESET);
        }
        
        return true; // 继续聊天
    }
} 