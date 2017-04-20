package com.yqc.chapter8;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestThreadNumber {
	public static void main(String[] args) {
		ThreadPoolExecutor tep = new ThreadPoolExecutor(0, 20, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100), new ThreadFactory() {

			@Override
			public Thread newThread(Runnable runnable) {
				return new Thread(runnable);
			}
		}, new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable arg0, ThreadPoolExecutor arg1) {
				throw new RuntimeException();
			}
		});
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				System.out.println("this is runnable! "+Thread.currentThread().getName());
			}
		};
		for (int i = 0; i < 99; i++) {
			tep.execute(runnable);
		}
	}
}
