package com.yqc;

import java.util.concurrent.locks.ReentrantLock;
/**
 * ReentrantLock在获取被阻塞时，并不会响应中断，只会设置线程的标志位
 *
 * @author yangqc
 * 2016年9月22日
 */
public class TestReentrantLockInterrupt {
	public static void main(String[] args) throws InterruptedException {
		ReentrantLock lock = new ReentrantLock();
		ReentrantLockTask task = new ReentrantLockTask(lock);
		Thread thread = new Thread(task);
		thread.start();
		lock.lock();
		thread.interrupt();
		lock.unlock();
		System.out.println("已解锁!");
	}
}

class ReentrantLockTask implements Runnable {
	private ReentrantLock lock;

	public ReentrantLockTask(ReentrantLock lock) {
		this.lock = lock;
	}

	@Override
	public void run() {
		while (true) {
			lock.lock();
			if(Thread.currentThread().isInterrupted()){
				break;
			}
			System.out.println("xixi");
			lock.unlock();
		}
	}
}
