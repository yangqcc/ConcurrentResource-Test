package com.yqc.testVolatile;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestVolatile {
	public static void main(String[] args) {
		Integer[] s = { 1, 2 };
		MyRunnable myRunnable = new MyRunnable(s);
		for (int i = 0; i < 10; i++) {
			new Thread(myRunnable).start();
		}
		s[0] = 12;
		System.out.println(s + "**");
	}
}

class MyRunnable implements Runnable {

	private Integer[] s;
	private volatile Apple apple;
	private AtomicInteger count = new AtomicInteger(0);

	public MyRunnable(Integer[] s) {
		this.s = s;
	}

	@Override
	public void run() {
		try {
			count.incrementAndGet();
			if (count.get() == 1) {
				apple = new Apple(10, s);
			}
			Thread.sleep(2000);
			System.out.println(Arrays.toString(apple.getS()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}