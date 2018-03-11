package com.yqc.basic.execute;

import java.util.concurrent.*;
import java.util.concurrent.RunnableFuture;

public class TestTask {
    public static void main(String[] args) throws Exception {
        Callable<Integer> callable = new Callable<Integer>() {
            int a = 0;

            @Override
            public Integer call() throws Exception {
                a++;
                System.out.println(Thread.currentThread().getName());
                return a;
            }
        };
        ExecutorService exec = Executors.newCachedThreadPool();
        FutureTask<Integer> ft = new FutureTask<>(callable);
        new Thread(ft) {
            public void run() {
                ft.run();
            }
        }.start();
        new Thread(ft) {
            public void run() {
                ft.run();
            }
        }.start();
        RunnableFuture<Integer> fr = (RunnableFuture<Integer>) exec.submit(callable);
        exec.shutdown();
        Thread.sleep(2000);
        System.out.println(ft.get());
    }
}
