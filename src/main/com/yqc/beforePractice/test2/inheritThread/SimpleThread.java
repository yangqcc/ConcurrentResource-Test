package com.yqc.beforePractice.test2.inheritThread;

/**
 * ¼Ì³ÐThread
 * 
 * @author yangqc 2016.5.12
 */
public class SimpleThread extends Thread {
	private int countDown = 5;
	private static int threadCount = 0;

	public SimpleThread() {
		super(Integer.toString(++threadCount));
		start();
	}

	public String toString() {
		return "#" + getName() + "(" + countDown + ")";
	}

	@Override
	public void run() {
		while (true) {
			System.out.println(this);
			if (--countDown == 0)
				return;
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < 5; i++) {
			new SimpleThread();
		}
	}
}
