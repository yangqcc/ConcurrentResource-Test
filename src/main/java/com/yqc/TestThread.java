package com.yqc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestThread {
	public static void main(String[] args) throws InterruptedException {
		NumberObject numberObject = new NumberObject(0);
		ReduceRunnable reduceRunnable = new ReduceRunnable(numberObject);
		AddRunnable addRunnable = new AddRunnable(numberObject);
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 10000; i++) {
			exec.execute(addRunnable);
		}
		for (int i = 0; i < 9000; i++) {
			exec.execute(reduceRunnable);
		}
		exec.shutdown();
		Thread.sleep(5000);
		System.out.println(numberObject.getCount());
	}
}

class ReduceRunnable implements Runnable {

	private NumberObject numberObject;

	public ReduceRunnable(NumberObject numberObject) {
		this.numberObject = numberObject;
	}

	@Override
	public void run() {
		numberObject.reduce();
	}
}

class AddRunnable implements Runnable {

	private NumberObject numberObject;

	public AddRunnable(NumberObject numberObject) {
		this.numberObject = numberObject;
	}

	@Override
	public void run() {
		numberObject.add();
	}
}

class NumberObject {
	private int count;

	public NumberObject(int count) {
		this.count = count;
	}

	public synchronized void reduce() {
		count++;
	}

	public synchronized void add() {
		count--;
	}

	public synchronized int getCount() {
		return count;
	}
}