package com.yqc.testLock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestLock {
	Lock lock = new ReentrantLock();
	private int a = 0;

	public static void main(String[] args) throws InterruptedException {
		TestLock testLock = new TestLock();
		ExecutorService executor = Executors.newCachedThreadPool();
		Runnable task1 = testLock.new Task();
		Runnable task2 = testLock.new Task1();
		// Future<?> future1 = executor.submit(task1);
		// Future<?> future2 = executor.submit(task2);
		Thread thread1 = new Thread(task1);
		Thread thread2 = new Thread(task2);
		thread1.start();
		thread2.start();
		Thread.sleep(1000);
		thread1.interrupt();
		thread2.interrupt();
	}

	class Task implements Runnable {
		@Override
		public void run() {
			while (true) {
				lock.lock();
				try {
					Thread.sleep(100);
					System.out.println(Thread.currentThread().getName() + "," + a++);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
	}

	class Task1 implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					lock.lockInterruptibly();
					Thread.sleep(100);
					System.out.println(Thread.currentThread().getName() + "," + a++);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					lock.unlock();
				}
			}
		}
	}
}
