package com.yqc.beforePractice.test2.synchronizedTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * synchronized解决线程同步问题
 * @author yangqc
 *
 */
public class MySychronized {
	private int count = 0;

	public synchronized int next() throws InterruptedException {
		count++;
		Thread.sleep(100);
		count++;
		return count;
	}

	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		MySychronized sy=new MySychronized();
		for(int i=0;i<3;i++)
			exec.execute(new MyRunnable(sy));
		exec.shutdown();
	}
}

class MyRunnable implements Runnable {

	private MySychronized mySychronized;

	public MyRunnable(MySychronized mySychronized) {
		this.mySychronized = mySychronized;
	}

	@Override
	public void run() {
		try {
			System.out.println(mySychronized.next());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}