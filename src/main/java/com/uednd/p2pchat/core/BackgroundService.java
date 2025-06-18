package com.uednd.p2pchat.core;

import lombok.Getter;

/**
 * 封装后台任务的抽象类，简化线程管理
 * 子类只需实现核心的task()方法，生命周期由本类管理
 * 
 * @version 1.0.0
 * @since 2025-06-11
 */
public abstract class BackgroundService {
    private Thread backgroundThread;

    @Getter
    private volatile boolean running = false;  // 多线程可见，初始状态为未启动

    /**
     * 子类必须实现此方法，它包含了在循环中要执行的核心逻辑
     * @throws Exception 如果任务执行出错，可以抛出异常
     */
    protected abstract void task() throws Exception;

    /**
     * 启动后台任务
     * <p>
     * 如果任务已经在运行，则此方法不执行任何操作
     */
    public void start() {
        if (running) 
            return; 
        
        // 状态设置为运行
        running = true;

        backgroundThread = new Thread(() -> {
            while (running) {
                try {
                    task();  // 创建消息接收线程（在MessageService中实现），用于持续监听消息
                } catch (Exception e) {
                    if (running) { 
                        System.err.println(getClass().getSimpleName() + " 后台任务失败: " + e.getMessage());
                    }
                    // 发生异常时停止任务，不然这里会无限循环打印错误
                    stop(); 
                }
            }
        });
        
        backgroundThread.setDaemon(true);  // 与主线程绑定，与主线程同时退出
        backgroundThread.start();
    }

    /**
     * 停止后台任务
     * <p>
     * 如果任务已经在运行，则此方法不执行任何操作
     */
    public void stop() {
        running = false;
        if (backgroundThread != null) {
            backgroundThread.interrupt();  // 中断线程，以防它在socket.receive中等待
            backgroundThread = null;  // 回收该线程
        }
    }

}