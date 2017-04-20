package com.yqc.testVolatile;

public class Apple {
	private final int i;
	private final Integer[] s;

	public Apple(int i, Integer[] s) {
		this.i = i;
		System.out.println(Thread.currentThread().getName());
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.s = s;
	}

	public Integer[] getS() {
		return s;
	}

	public int getI() {
		return i;
	}
}
