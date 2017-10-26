package com.yqc.resource;

import java.util.concurrent.TimeUnit;

/**
 * java��������ԭ���Ե�
 *
 * @author yangqc
 * 2016��7��21��
 */
public class EvenGenerator extends IntGenerator{
	private int currentEvenValue=0;

	@Override
	public int next() {
		++currentEvenValue;   //�������ִ���
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
		EvenChecker.test(new EvenGenerator());
	}

}
