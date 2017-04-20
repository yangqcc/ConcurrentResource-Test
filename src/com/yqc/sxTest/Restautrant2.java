package com.yqc.sxTest;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 生产者，消费者问题
 *
 * @author yangqc 2016年9月16日
 */
class MyMealsContainer {
	private LinkedList<Meal> meals;
	private int mealNum;

	public MyMealsContainer(int mealNum) {
		if ((this.mealNum = mealNum) < 0) {
			throw new IllegalArgumentException("数量不能为0!");
		}
		meals = new LinkedList<>();
	}

	public synchronized void put(Meal meal) throws InterruptedException {
		while (meals.size() == mealNum) {
			wait();
		}
		meals.addLast(meal);
		System.out.println(Thread.currentThread().getName() + "-- put:" + meals.size());
		notifyAll();
	}

	public synchronized Meal get() throws InterruptedException {
		while (meals.size() <= 0) {
			wait();
		}
		Meal meal = meals.removeFirst();
		notifyAll();
		return meal;
	}

	public synchronized int getNum() {
		return meals.size();
	}
}

class Chief1 implements Runnable {
	private MyMealsContainer mealContainer;
	private AtomicInteger count;

	public Chief1(MyMealsContainer mealContainer, AtomicInteger count) {
		this.mealContainer = mealContainer;
		this.count = count;
	}

	@Override
	public void run() {
		while (true) {
			try {
				Meal meal;
				synchronized (mealContainer) {
					if (count.get() == 100) {
						break;
					}
					meal = new Meal(count.addAndGet(1));
					mealContainer.put(meal);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Waiter1 implements Runnable {
	private MyMealsContainer mealContainer;

	Waiter1(MyMealsContainer mealContainer) {
		this.mealContainer = mealContainer;
	}

	@Override
	public void run() {
		while (true) {
			try {
				System.out.println(Thread.currentThread().getName() + "-- get:" + mealContainer.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

public class Restautrant2 {
	public static void main(String[] args) throws InterruptedException {
		int containerSize = 1;
		MyMealsContainer mealContainers = new MyMealsContainer(containerSize);
		ExecutorService exec = Executors.newCachedThreadPool();
		AtomicInteger count = new AtomicInteger(0);
		for (int i = 0; i < 10; i++) {
			exec.execute(new Waiter1(mealContainers));
			exec.execute(new Chief1(mealContainers, count));
		}
		Thread.sleep(5000);
		System.out.println(mealContainers.getNum() + "**");
	}
}
