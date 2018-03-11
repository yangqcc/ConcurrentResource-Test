package com.yqc.beforepractice.thinkinjava;

import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

/**
 * 异常逃逸，捕获不到
 * 2015.12.30
 *
 * @author Administrator
 */
public class ExceptionThread implements Runnable {

    public static void main(String[] args) {
        try {
            ExecutorService exec = Executors.newCachedThreadPool();
            exec.execute(new ExceptionThread());
        } catch (Exception e) {
            System.out.println("Exception has been handled!");
        }
    }

    @Override
    public void run() {
        throw new RuntimeException();
    }
}
