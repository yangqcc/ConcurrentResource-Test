package com.yqc.beforepractice.test2.ThreadException;

import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

/**
 * �߳��쳣����
 *
 * @author yangqc
 */
public class SettingDefaultHandler {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.execute(new ExceptionThread());
    }
}
