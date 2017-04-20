package com.yqc.endThread;
/**
 * 一个任务能够调用同一个对象的其他的sunchronized方法，而这个任务已经持有锁了
 *
 * @author yangqc
 * 2016年7月25日
 */
public class MultiLock {
	public synchronized void f1(int count) {
		if (count-- > 0) {
			System.out.println("f1() calling f2() with count " + count);
			f2(count);
		}
	}

	public synchronized void f2(int count) {
		if (count-- > 0) {
			System.out.println("f2() calling f1() with count " + count);
			f1(count);
		}
	}

	public static void main(String[] args) {
		final MultiLock multiLock = new MultiLock();
		new Thread() {
			public void run() {
				multiLock.f1(10);
			}
		}.start();
	}
}
