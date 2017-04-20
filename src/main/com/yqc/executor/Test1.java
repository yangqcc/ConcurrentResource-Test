package com.yqc.executor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 测试饱和策略，当线程池满后，并且有界队列被填满后，开始执行饱和策略，setRejectedExecutionPool实现，
 * 默认是抛出未检查的RejectedExecutionException 
 * (终止饱和策略)
 * 
 * @author yangqc 2016年9月4日
 */
public class Test1 {
	public static void main(String[] args) {
		ThreadPoolExecutor threadPool = new ThreadPoolExecutor(0, 5, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(20));
		for (int i = 0; i < 8; i++) {
			threadPool.execute(new MyRunnable());
		}
		threadPool.shutdown();
	}
}

class MyRunnable implements Runnable {

	@Override
	public void run() {
		System.out.println(Thread.currentThread().getName());
	}
}
