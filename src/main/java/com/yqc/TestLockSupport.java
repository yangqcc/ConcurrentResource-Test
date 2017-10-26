package com.yqc;

import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport会响应中断，也就是说，调用park，该线程阻塞，如果此期间 线程被中断，那么会从阻塞中恢复，但是只会设置中断标志位，不会抛出异常
 * 这时候需要我们设置中断响应策略
 *
 * @author yangqc 2016年9月21日
 */
public class TestLockSupport {
	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread(new LockSupportTask());
		thread.start();
		Thread.sleep(1000);
		thread.interrupt();
		// LockSupport.unpark(thread);
	}
}

class LockSupportTask implements Runnable {

	@Override
	public void run() {
		int i = 0;
		while (true) {
			try {
				if (Thread.currentThread().isInterrupted()) {
//					break;
				}
				if(i==0){
					Thread.sleep(100);
				}
				System.out.println("哎，我要被阻塞了！");
				if (i == 0) {
					LockSupport.park();
				}
				i++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("xixi");
		}
//		System.out.println("娘的，老子被中断了!");
	}
}