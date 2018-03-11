package com.yqc.beforepractice.thinkinjava;

import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

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
