package com.uednd.p2pchat.ui.cli.command;

import java.util.List;
import java.util.Scanner;

import com.uednd.p2pchat.model.User;
import com.uednd.p2pchat.repository.ChatRepository;
import com.uednd.p2pchat.service.FileTransferService;
import com.uednd.p2pchat.service.MessageService;
import com.uednd.p2pchat.ui.cli.util.MenuDisplay;
import com.uednd.p2pchat.util.ANSIcolor;

/**
 * 帮助命令实现
 * <p>
 * 显示所有可用命令及其描述
 * 
 * @version 1.0.0
 * @since 2025-06-17
 */
public class HelpCommand implements Command {
    
    private final List<Command> commands;
    
    /**
     * 构造帮助命令
     * @param commands 所有可用命令列表
     */
    public HelpCommand(List<Command> commands) {
        this.commands = commands;
    }
    
    @Override
    public String getName() {
        return "help";
    }
    
    @Override
    public String getDescription() {
        return "显示帮助菜单";
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
        System.out.println();
        MenuDisplay.printSeparator("可用命令");
        
        // 显示所有命令
        for (Command cmd : commands) {
            System.out.println(ANSIcolor.YELLOW + " /" + cmd.getName() + ANSIcolor.RESET + ANSIcolor.WHITE + " - " + cmd.getDescription() + ANSIcolor.RESET);
        }
        
        return true; // 继续聊天
    }
} 