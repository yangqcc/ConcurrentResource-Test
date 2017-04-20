package com.yqc.chapter10;
/**
 * 容易发生死锁
 *
 * @author yangqc
 * 2016年8月14日
 */
public class LeftRightDeadLock {
	private final Object left = new Object();
	private final Object right = new Object();

	public void leftRight() {
		synchronized (left) {
			synchronized (right) {
				System.out.println("leftRight!");
			}
		}
	}

	public void rightLeft() {
		synchronized (right) {
			synchronized (left) {
				System.out.println("rightLeft");
			}
		}
	}
}
