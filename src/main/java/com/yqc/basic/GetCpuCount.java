package com.yqc.basic;

/**
 * 获取当前pc的cpu核心数
 * Created by yangqc on 2017/10/29
 */
public class GetCpuCount {

    public static void main(String[] args) {
        System.out.println(Runtime.getRuntime().availableProcessors());
        Thread thread = new Thread(() -> {
            System.out.println(Thread.currentThread().getName());
        });
        Runtime.getRuntime().addShutdownHook(thread);
        thread.start();
    }
}
