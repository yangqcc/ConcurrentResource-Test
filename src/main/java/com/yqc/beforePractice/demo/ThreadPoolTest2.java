package com.yqc.beforePractice.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolTest2 {
    public static void main(String[] args) {
//		ExecutorService threadPool=Executors.newFixedThreadPool(3);
//		ExecutorService threadPool=Executors.newCachedThreadPool();
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        for (int l = 1; l <= 10; l++) {
            final int task = l;
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 10; i++) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(Thread.currentThread().getName() + " is loop of " + i + " task of " + task);
                    }
                }
            });
        }
        System.out.println("all of 10 tasks have committed!");
//		threadPool.shutdown();

        Executors.newScheduledThreadPool(3).schedule(new Runnable() {
            @Override
            public void run() {
                System.out.println("boming!");
            }
        }, 10, TimeUnit.SECONDS);

        Executors.newScheduledThreadPool(3).scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                System.out.println("hello!");
            }

        }, 2, 1, TimeUnit.SECONDS);
    }
}
