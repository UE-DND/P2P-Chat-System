package com.uednd.p2pchat.ui.cli.command;

import java.util.Scanner;

import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.service.FileTransferService;
import com.uednd.p2pchat.service.MessageService;
import com.uednd.p2pchat.ui.cli.util.InputHandler;
import com.uednd.p2pchat.ui.cli.util.MenuDisplay;
import com.uednd.p2pchat.util.ANSIcolor;

/**
 * 清除命令实现
 * <p>
 * 用于删除聊天记录
 * 
 * @version 1.0.0
 * @since 2025-06-08
 */
public class ClearCommand implements Command {
    
    @Override
    public String getName() {
        return "clear";
    }
    
    @Override
    public String getDescription() {
        return "删除聊天记录";
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
        // 获取确认
        System.out.println(ANSIcolor.YELLOW + "此操作将清空与 " + oppositeUser.getUsername() + " 的所有聊天记录。" + ANSIcolor.RESET);
        boolean confirm = InputHandler.getConfirmation(scanner, "确定要清空聊天记录吗?");
        
        if (!confirm) {
            System.out.println(ANSIcolor.YELLOW + "操作已取消。" + ANSIcolor.RESET);
            return true;
        }
        
        try {
            // 清除聊天记录
            messageService.clearChatHistory();
            System.out.println(ANSIcolor.GREEN + "聊天记录已清空。" + ANSIcolor.RESET);
            
            // 清屏并打印分隔符
            MenuDisplay.clearScreen();
            MenuDisplay.printSeparator("与 " + oppositeUser.getUsername() + " 对话");
            System.out.println(ANSIcolor.YELLOW + " (输入 /help 查看可用命令)" + ANSIcolor.RESET);
            MenuDisplay.printSeparator(null);
        } catch (Exception e) {
            System.out.println(ANSIcolor.RED + "删除聊天记录失败: " + e.getMessage() + ANSIcolor.RESET);
        }
        
        return true; // 继续聊天
    }
} 