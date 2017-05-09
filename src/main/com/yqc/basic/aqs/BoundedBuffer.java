package com.yqc.basic.aqs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BoundedBuffer<V> extends BaseBoundedBuffer<V> {

	protected BoundedBuffer(int capacity) {
		super(capacity);
	}

	public synchronized void put(V v) throws InterruptedException {
		while (isFull())
			wait();
		doPut(v);
		notifyAll();
	}

	public synchronized V take() throws InterruptedException {
		while (isEmpty())
			wait();
		V v = doTake();
		notifyAll();
		return v;
	}

	public static void main(String[] args) throws InterruptedException {
		BoundedBuffer<Integer> bb = new BoundedBuffer<>(5);
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++) {
			exec.execute(new Producer(bb));
		}
//		for (int i = 0; i < 5; i++) {
//			exec.execute(new Consumer(bb));
//		}
		exec.shutdown();
	}
}

class Producer implements Runnable {

	private BoundedBuffer<Integer> bb;

	public Producer(BoundedBuffer<Integer> bb) {
		this.bb = bb;
	}

	@Override
	public void run() {
		try {
			while (true) {
				if (Thread.currentThread().isInterrupted()) {
					System.out.println("*************8");
					return;
				}
				bb.put(1);
				System.out.println(Thread.currentThread().getName() + ",我是生产者!");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException("我被中断了!");
		}
	}
}

class Consumer implements Runnable {

	private BoundedBuffer<Integer> bb;

	public Consumer(BoundedBuffer<Integer> bb) {
		this.bb = bb;
	}

	@Override
	public void run() {
		while (true) {
			if (Thread.currentThread().isInterrupted()) {
				return;
			}
			bb.doTake();
			System.out.println(Thread.currentThread().getName() + ",我是消费者!");
		}
	}
}