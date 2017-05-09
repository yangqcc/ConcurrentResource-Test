package com.yqc.beforePractice.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolTest {
	public static void main(String[] args) {
		ExecutorService threadPool=Executors.newFixedThreadPool(3);
		for (int l = 1; l <= 10; l++) {
			final int task=l;
			threadPool.execute(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < 10; i++) {
						try {
							Thread.sleep(20);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println(Thread.currentThread().getName()+ " is loop of " + i+" task of "+task);
					}
				}
			});
		}
		System.out.println("all of 10 tasks have committed!");
		threadPool.shutdown();
	}
}
