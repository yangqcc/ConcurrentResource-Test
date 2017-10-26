package com.yqc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestCallable {
	public static void main(String[] args) {
		int[] a = new int[] { 1, 2, 3, 4 };
		ExecutorService exec = Executors.newCachedThreadPool();
		MyCallable myCallable = new MyCallable(a);
		List<Future<Integer>> futures = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			Future<Integer> f;
			futures.add(f = exec.submit(myCallable));
//			f.cancel(true);
		}
		exec.shutdown();
		for (int i = 0; i < 100; i++) {
			futures.get(i).cancel(true);
		}
		for (int i = 0; i < 100; i++) {
			try {
				System.out.println(i + ":" + futures.get(i).isCancelled());
				System.out.println(i + ":" + futures.get(i).get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
	}
}

class MyCallable implements Callable<Integer> {
	private final int a[]; // ��Ϊ˽�б��������ⲻ��ȫ����

	public MyCallable(int a[]) {
		this.a = a;
	}

	@Override
	public Integer call() throws Exception {
		int sum = 0;
		synchronized (this) {
			for (int i = 0; i < a.length; i++) {
				sum += a[i];
			}
		}
		return sum;
	}
}