package com.yqc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * �����Ĵ��룬д�úܲ�
 *
 * @author yangqc 2016��9��21��
 */
public class BirthdayService {
	public static void main(String[] args) {
		BlockingQueue<AnotherMyTask> queue=new ArrayBlockingQueue<>(20);
		ExecutorService exec=Executors.newCachedThreadPool();
		exec.execute(new Producter(queue));
		exec.execute(new Worker(queue));
		exec.shutdown();
	}
}

class AnotherMyTask {
	private int[] a;

	public AnotherMyTask(int[] a) {
		if (a == null || a.length == 0) {
			throw new IllegalArgumentException();
		}
		this.a = a;
	}

	public void conmulity() {
		int sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += a[i];
		}
		System.out.println(sum);
	}
}

class Producter implements Runnable{
	final BlockingQueue<AnotherMyTask> queue;

	public Producter(BlockingQueue<AnotherMyTask> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while (true) {
			try {
//				Thread.sleep(100);
				queue.put(new AnotherMyTask(new int[]{1,2,3,4,5}));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Worker implements Runnable {

	final BlockingQueue<AnotherMyTask> queue;

	public Worker(BlockingQueue<AnotherMyTask> queue) {
		this.queue = queue;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				queue.take().conmulity();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}