package com.yqc.util.countdownlatch;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Work implements Runnable {

    private CountDownLatch countDownLatch;
    private String name;

    public Work(CountDownLatch countDownLatch, String name) {
        this.countDownLatch = countDownLatch;
        this.name = name;
    }

    @Override
    public void run() {
        this.doWork();
        try {
            Thread.sleep(new Random().nextInt(10));
        } catch (InterruptedException e) {
            System.out.println("出错了!");
        }
        System.out.println(this.name + ",活干完了!");
        this.countDownLatch.countDown();
    }

    private void doWork() {
        System.out.println(this.name + "正在干活!");
    }
}
