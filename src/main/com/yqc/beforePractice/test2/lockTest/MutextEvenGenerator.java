package com.yqc.beforePractice.test2.lockTest;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.yqc.getResource.EventChecker;
import com.yqc.getResource.IntGenerator;

public class MutextEvenGenerator extends IntGenerator {
	private int currentEvenValue = 0;
	private Lock lock = new ReentrantLock();

	@Override
	public int next() {
		lock.lock();
		try {
			++currentEvenValue;
//			Thread.yield();
			return currentEvenValue;
		} finally {
			lock.unlock();
		}
	}
	
	public static void main(String[] args) {
		EventChecker.test(new MutextEvenGenerator());
	}
}
