package com.yqc.Resource;
/**
 * 两个线程可以同时进入一个对象，只要两个方法持有两个不同的锁
 *
 * @author yangqc
 * 2016年7月24日
 */
class DualSynch {
	//该对象两个锁是相互独立的
	private Object syncObject = new Object();

	public synchronized void f() {
		for (int i = 0; i < 5; i++) {
			System.out.println("f()");
			Thread.yield();
		}
	}

	public void g() {
		synchronized (syncObject) {
			for (int i = 0; i < 5; i++) {
				System.out.println("g()");
				Thread.yield();
			}
		}
	}
}

public class SyncObject {
	public static void main(String[] args) {
		final DualSynch ds = new DualSynch();
		new Thread() {
			public void run() {
				ds.f();
			}
		}.start();
		ds.g();
	}
}
