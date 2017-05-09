package com.yqc.resource;

import java.util.concurrent.TimeUnit;

public class SychronizedEvenGenerator extends IntGenerator{
	private int currentEvenValue=0;

	@Override
	public synchronized int next() {
		++currentEvenValue;   //这里会出现错误
		Thread.yield();
		try {
			TimeUnit.MICROSECONDS.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		++currentEvenValue;
		return currentEvenValue;
	}

	public static void main(String[] args) {
		EvenChecker.test(new SychronizedEvenGenerator());
	}
}
