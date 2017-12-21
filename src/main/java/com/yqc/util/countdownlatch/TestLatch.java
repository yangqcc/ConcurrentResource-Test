package com.yqc.util.countdownlatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 闭锁
 */
public class TestLatch {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(3);
        Work w1 = new Work(latch, "张三");
        Work w2 = new Work(latch, "李四");
        Work w3 = new Work(latch, "王二");

        Boss boss = new Boss(latch);
        executorService.execute(w1);
        executorService.execute(w2);
        executorService.execute(w3);
        executorService.execute(boss);
        executorService.shutdown();
    }
}
