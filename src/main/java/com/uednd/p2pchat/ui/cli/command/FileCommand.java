package com.uednd.p2pchat.ui.cli.command;

import java.io.File;
import java.util.Scanner;

import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.service.FileTransferService;
import com.uednd.p2pchat.service.MessageService;
import com.uednd.p2pchat.ui.cli.util.InputHandler;
import com.uednd.p2pchat.util.ANSIcolor;

/**
 * 文件命令实现
 * <p>
 * 用于发送文件
 * 
 * @version 1.0.0
 * @since 2025-06-17
 */
public class FileCommand implements Command {
    
    @Override
    public String getName() {
        return "file";
    }
    
    @Override
    public String getDescription() {
        return "发送文件";
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
        System.out.println(ANSIcolor.YELLOW + "请输入要发送的文件路径: " + ANSIcolor.RESET);
        String filePath = scanner.nextLine().trim();
        
        // 如果文件路径为空，则返回true继续聊天
        if (filePath.isEmpty()) {
            System.out.println(ANSIcolor.RED + "文件路径不能为空。" + ANSIcolor.RESET);
            return true;
        }
        
        // 如果文件不存在或不是有效文件，则返回true继续聊天
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println(ANSIcolor.RED + "文件不存在或不是有效文件: " + filePath + ANSIcolor.RESET);
            return true;
        }
        
        // 确认发送
        boolean confirm = InputHandler.getConfirmation(scanner, "确定要发送文件 \"" + file.getName() + "\" 吗?");
        
        if (!confirm) {
            System.out.println(ANSIcolor.YELLOW + "文件发送已取消。" + ANSIcolor.RESET);
            return true;
        }
        
        try {
            System.out.println(ANSIcolor.CYAN + "正在发送文件..." + ANSIcolor.RESET);
            fileTransferService.sendFile(filePath);
            System.out.println(ANSIcolor.GREEN + "文件已成功发送!" + ANSIcolor.RESET);
        } catch (Exception e) {
            System.out.println(ANSIcolor.RED + "发送文件失败: " + e.getMessage() + ANSIcolor.RESET);
        }
        
        return true;  // 继续聊天
    }
} 