package com.yqc.beforePractice.test2.lockTest;

import java.util.concurrent.atomic.AtomicInteger;

import com.yqc.getResource.EventChecker;
import com.yqc.getResource.IntGenerator;

public class AtomicEvenGenerator extends IntGenerator{

	private AtomicInteger currentEvenValue=new AtomicInteger(0);
	@Override
	public int next() {
		return currentEvenValue.addAndGet(2);
	}
	public static void main(String[] args) {
		EventChecker.test(new AtomicEvenGenerator());
	}
}
