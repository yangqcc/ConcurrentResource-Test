package com.yqc.beforePractice.thinkinjava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VolatileTest implements Runnable {
    public volatile int i = 1;

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        VolatileTest at = new VolatileTest();
        exec.execute(at);
    }

    @Override
    public void run() {
        i = i + 1;
        System.out.println(i);
    }
}
