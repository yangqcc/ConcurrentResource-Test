package com.yqc.practice.chapter11;

import java.util.concurrent.BlockingQueue;
/**
 * 访问任务队列时，是串行访问
 *
 * @author yangqc
 * 2016年8月16日
 */
public class WorkerThread extends Thread {
	private final BlockingQueue<Runnable> queue;

	public WorkerThread(BlockingQueue<Runnable> queue) {
		this.queue = queue;
	}

	public void run() {
		while (true) {
			try {
				Runnable task = queue.take();
				task.run();
			} catch (InterruptedException e) {
				break; // 允许线程退出
			}
		}
	}
}
