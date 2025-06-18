package com.uednd.p2pchat.ui.cli.util;

import java.util.Scanner;
import com.uednd.p2pchat.util.ANSIcolor;

/**
 * 输入处理类
 * <p>
 * 用于处理用户输入，确保合法性
 * 
 * @version 1.0.0
 * @since 2025-06-07
 */
public class InputHandler {

    /**
     * 等待用户按 Enter 键继续
     * @param scanner 输入扫描器
     * @param prompt 提示信息
     */
    public static void waitForEnter(Scanner scanner, String prompt) {
        System.out.print(ANSIcolor.YELLOW + prompt + ANSIcolor.RESET);
        scanner.nextLine();
    }

    /**
     * 获取用户的确认 (y/n)
     * @param scanner 输入扫描器
     * @param prompt 提示信息
     * @return 如果用户输入 'y' 则返回 true，否则返回 false
     */
    public static boolean getConfirmation(Scanner scanner, String prompt) {
        String input;
        while (true) {
            System.out.print(ANSIcolor.YELLOW + prompt + ANSIcolor.RESET);
            input = scanner.nextLine().trim();
            if ("y".equalsIgnoreCase(input)) {
                return true;
            } else if ("n".equalsIgnoreCase(input)) {
                return false;
            } else {
                System.out.println(ANSIcolor.RED + "无效输入，请输入 'y' 或 'n'." + ANSIcolor.RESET);
            }
        }
    }

    /**
     * 获取有效输入
     * 
     * @param scanner 输入流
     * @param fieldName 用户输入
     * @param errorMessage 错误消息
     * @return 有效的字符串
     */
    public static String getValidInput(Scanner scanner, String fieldName, String errorMessage) {
        String input = "";
        boolean validInput = false;
        
        while (!validInput) {
            try {
                System.out.print(ANSIcolor.YELLOW + " " + fieldName + ": " + ANSIcolor.RESET);
                input = scanner.nextLine();
                // 删除空白字符后为空
                if (input.trim().isEmpty()) {
                    throw new IllegalArgumentException(errorMessage);
                }
                
                validInput = true;
            } catch (IllegalArgumentException e) {
                System.out.println(ANSIcolor.RED + " ⚠️ 错误: " + ANSIcolor.YELLOW + e.getMessage() + ANSIcolor.RESET);
            } catch (Exception e) {
                System.out.println(ANSIcolor.RED + " ⚠️ 发生未知错误: " + ANSIcolor.YELLOW + e.getMessage() + ANSIcolor.RESET);
                System.out.println(ANSIcolor.RED + " ⚠️ 系统已退出" + ANSIcolor.RESET);
                scanner.close();
                System.exit(1);
            }
        }
        
        return input;
    }
} 