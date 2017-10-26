package com.yqc.beforePractice.test2.ThreadException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程异常处理
 *
 * @author yangqc
 *
 */
public class SettingDefaultHandler {
	public static void main(String[] args) {
		Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(new ExceptionThread());
	}
}
