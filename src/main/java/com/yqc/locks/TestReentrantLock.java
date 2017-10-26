package com.yqc.locks;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 测试可重入锁
 *
 * @author yangqc 2016年8月27日
 */
public class TestReentrantLock {
	private ArrayList<Integer> arrayList = new ArrayList<>();
	Lock lock = new ReentrantLock();   //多个线程共享一个锁

	public void insert(Thread thread) {
		// Lock lock = new ReentrantLock(); //每个线程都有自己的锁
		lock.lock();
		try {
			System.out.println(thread.getName() + "得到锁!");
			for (int i = 0; i < 5; i++) {
				arrayList.add(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.println(thread.getName() + "释放锁!");
			lock.unlock();
		}
	}

	public static void main(String[] args) {
		final TestReentrantLock test = new TestReentrantLock();
		new Thread() {
			public void run() {
				test.insert(Thread.currentThread());
			}
		}.start();

		new Thread() {
			public void run() {
				test.insert(Thread.currentThread());
			};
		}.start();
	}
}
