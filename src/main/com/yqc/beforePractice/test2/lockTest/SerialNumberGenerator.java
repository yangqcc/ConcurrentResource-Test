package com.yqc.beforePractice.test2.lockTest;

/**
 * java递增操作不是原子性的，涉及一个读操作和写操作
 * 
 * @author yangqc
 * 
 */
public class SerialNumberGenerator {
	private static volatile int serialNumber = 0;

	public static int nextSerialNumber() {
		return serialNumber++;
	}
}
