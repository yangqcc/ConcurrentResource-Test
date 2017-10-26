package com.yqc.bankTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyBank {
	private int count;

	public MyBank(int count) {
		this.count = count;
	}

	public synchronized void delete() throws InterruptedException {
		count--;
		while (count > 0) {
			System.out.println(Thread.currentThread().getName() + "  等待!");
			wait();
		}
		System.out.println("释放了!");
		notifyAll();
	}

	public static void main(String[] args) {
		MyBank myBank = new MyBank(10);
		MyRunnable myRunnable = new MyRunnable(myBank);
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 10; i++) {
			exec.execute(myRunnable);
		}
		// exec.shutdown();
	}
}

class MyRunnable implements Runnable {

	private MyBank myBank;

	MyRunnable(MyBank myBank) {
		this.myBank = myBank;
	}

	@Override
	public void run() {
		try {
			myBank.delete();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}