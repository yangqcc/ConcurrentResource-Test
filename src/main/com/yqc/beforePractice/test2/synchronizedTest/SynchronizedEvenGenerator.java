package com.yqc.beforePractice.test2.synchronizedTest;

import com.yqc.getResource.EventChecker;
import com.yqc.getResource.IntGenerator;

public class SynchronizedEvenGenerator extends IntGenerator{

	private int currentEvenValue=0;
	@Override
	public synchronized int next() {
		++currentEvenValue;
		Thread.yield();
		++currentEvenValue;
		return currentEvenValue;
	}

	public static void main(String[] args) {
		EventChecker.test(new SynchronizedEvenGenerator());
	}
}
