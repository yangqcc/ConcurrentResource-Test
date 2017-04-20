package com.yqc.chapter6;

import java.util.concurrent.CountDownLatch;

public class TestHarness {
	public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(nThreads);
		for (int i = 0; i < nThreads; i++) {
			Thread t = new Thread() {
				public void run() {
					try {
						startGate.await();
						try {
							task.run();
						} finally {
							endGate.countDown();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		}
		long start = System.nanoTime();
		startGate.countDown(); // 启动门使主线程同时释放所有线程，开启工作
		endGate.await(); // 结束门是主线程等待所有的工作线程完成
		long end = System.nanoTime();
		return end - start;
	}

	public static void main(String[] args) throws InterruptedException {
		TestHarness test = new TestHarness();
		System.out.println(test.timeTasks(10, new MyRunnable()));
	}
}

class MyRunnable implements Runnable {

	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName());
	}
}