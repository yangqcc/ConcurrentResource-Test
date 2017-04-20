package com.yqc;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockList {
	private Object[] objs;
	private int size;
	private Lock lock = new ReentrantLock();
	private Condition notFull = lock.newCondition();
	private Condition notEmpty = lock.newCondition();
	private int currentPosition = 0;
	private int getPosition = 0;
	private int count = 0;

	public MyBlockList(int size) {
		this.size = size;
		objs = new Object[size];
	}

	public void put(Object obj) {
		lock.lock();
		try {
			while (true) {
				if (count < size) {
					objs[currentPosition] = obj;
					if (++currentPosition == size)
						currentPosition = 0;
					notFull.signalAll();
					count++;
					break;
				} else {
					notEmpty.await();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}

	public String toString() {
		return Arrays.toString(objs);
	}

	public Object get() {
		Object value = null;
		lock.lock();
		try {
			while (true) {
				if (count > 0) {
					value = objs[getPosition];
					objs[getPosition] = null;
					if (++getPosition == size) {
						getPosition = 0;
					}
					notEmpty.signalAll();
					count--;
					break;
				} else {
					notFull.await();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
		return value;
	}

	public static void main(String[] args) throws InterruptedException {
		MyBlockList list = new MyBlockList(3);
		// ArrayBlockingQueue<Integer> list=new ArrayBlockingQueue<>(3);
		ExecutorService exec = Executors.newCachedThreadPool();
		Runnable getRunnable = new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				list.put(1);
				System.out.println(list + "*put()");
			}
		};
		Runnable putRunnable = new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				list.get();
				System.out.println(list + "*get()");
			}
		};
		exec.execute(putRunnable);
		exec.execute(putRunnable);
		exec.execute(putRunnable);
		exec.execute(getRunnable);
		exec.execute(getRunnable);
		exec.execute(getRunnable);
		Thread.sleep(5000);
		System.out.println(list);
		System.out.println(list.count);
	}
}
