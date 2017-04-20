package com.yqc.semaphore;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TestSemaphore {
	private static int COUNT = 3;
	Semaphore semaphore = new Semaphore(COUNT);
	Semaphore notEmpty = new Semaphore(0);
	Object[] objs = new Object[COUNT];
	private int getIndex = 0;
	private int putIndex = 0;
	Lock lock = new ReentrantLock();

	public Object get() {
		Object value = null;
		try {
			notEmpty.acquire();
			lock.lock();
			objs[getIndex] = null;
			if (++getIndex == COUNT) {
				getIndex = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
			semaphore.release();
		}
		return value;
	}

	public void put(Object obj) {
		try {
			semaphore.acquire();
			lock.lock();
			objs[putIndex] = obj;
			if (++putIndex == COUNT) {
				putIndex = 0;
			}
			System.out.println(Arrays.toString(objs));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
			notEmpty.release();
		}

	}

	public static void main(String[] args) throws InterruptedException {
		final TestSemaphore testSemaphore = new TestSemaphore();
		ExecutorService exec = Executors.newCachedThreadPool();
		Runnable getRunnable = new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					testSemaphore.get();
				}
			}
		};
		Runnable putRunnable = new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (Thread.currentThread().isInterrupted()) {
						break;
					}
					testSemaphore.put(1);
				}
			}
		};
		exec.execute(getRunnable);
		exec.execute(getRunnable);
		exec.execute(getRunnable);
		exec.execute(putRunnable);
		exec.execute(putRunnable);
		exec.execute(putRunnable);
		Thread.sleep(5000);
		exec.shutdownNow();
		System.out.println(Arrays.toString(testSemaphore.objs));
	}
}
