package java.util.practice.chapter6;

import java.util.concurrent.CountDownLatch;

public class TestHarness {
	public long timeTasks(int nThreads, final Runnable task) throws InterruptedException {
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(nThreads);
		for (int i = 0; i < nThreads; i++) {
			Thread t = new Thread() {
				public void run() {
					try {
						startGate.await();
						try {
							task.run();
						} finally {
							endGate.countDown();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
		}
		long start = System.nanoTime();
		startGate.countDown(); // ������ʹ���߳�ͬʱ�ͷ������̣߳���������
		endGate.await(); // �����������̵߳ȴ����еĹ����߳����
		long end = System.nanoTime();
		return end - start;
	}

	public static void main(String[] args) throws InterruptedException {
		TestHarness test = new TestHarness();
		System.out.println(test.timeTasks(10, new MyRunnable()));
	}
}

class MyRunnable implements Runnable {

	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName());
	}
}