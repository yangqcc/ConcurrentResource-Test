package com.yqc;

import java.util.concurrent.locks.ReentrantLock;
/**
 * ReentrantLock�ڻ�ȡ������ʱ����������Ӧ�жϣ�ֻ�������̵߳ı�־λ
 *
 * @author yangqc
 * 2016��9��22��
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
		System.out.println("�ѽ���!");
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
