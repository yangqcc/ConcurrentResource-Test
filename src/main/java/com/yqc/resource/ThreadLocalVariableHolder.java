package com.yqc.resource;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * �������ش洢���Խ����Դ��������
 *
 * @author yangqc 2016��7��24��
 */
class Accessor implements Runnable {
	private final int id;

	public Accessor(int idn) {
		this.id = idn;
	}

	@Override
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			ThreadLocalVariableHolder.increment();
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(this);
//			Thread.yield();
		}
	}

	public String toString() {
		return "#" + id + ":" + ThreadLocalVariableHolder.get();
	}
}

public class ThreadLocalVariableHolder {
	private static int b=0;
	private static int a = b++;
	private static ThreadLocal<Integer> value = new ThreadLocal<Integer>() {
		private Random rand = new Random(47);

		protected synchronized Integer initialValue() {
			return rand.nextInt(10000);  //ÿ�η��ز�һ����ֵ
		}
	};
	static{
		System.out.println(a);
	}

	public static void increment() {
		value.set(value.get() + 1);
	}

	public static int get() {
		return value.get();
	}

	public static void main(String[] args) throws InterruptedException {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++) {
			exec.execute(new Accessor(i));
		}
		TimeUnit.SECONDS.sleep(3);
		exec.shutdown();
	}
}
