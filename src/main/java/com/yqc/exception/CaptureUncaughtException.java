package com.yqc.exception;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * 捕获线程异常
 *
 * @author yangqc 2016年7月21日
 */
class ExceptionThread2 implements Runnable {  //定义一个任务

    @Override
    public void run() {
        Thread t = Thread.currentThread();
        System.out.println("*run() by " + t);
        System.out.println(" eh = " + t.getUncaughtExceptionHandler());
        throw new RuntimeException();
    }

}

class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {  //定义自己的异常

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("cautht" + e);
    }
}

class HandlerThreadFactory implements ThreadFactory { //线程工厂

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        System.out.println("eh=" + t.getUncaughtExceptionHandler());
        return t;
    }
}

public class CaptureUncaughtException {
    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool(new HandlerThreadFactory());
        exec.execute(new ExceptionThread2());
//		exec.shutdown();
    }
}
