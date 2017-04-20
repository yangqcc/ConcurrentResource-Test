package com.yqc.chapter8;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
/**
 * 天假日志和计时等功能的线程池
 *
 * @author yangqc
 * 2016年8月7日
 */
public class TimingThreadPool extends ThreadPoolExecutor {
	public TimingThreadPool(int arg0, int arg1, long arg2, TimeUnit arg4, BlockingQueue<Runnable> arg5) {
		super(arg0, arg1, arg2, arg4, arg5);
	}

	private final ThreadLocal<Long> startTime = new ThreadLocal<>();
	private final Logger log = Logger.getLogger("TimingThreadPool");
	private final AtomicLong numTasks = new AtomicLong();
	private final AtomicLong totalTime = new AtomicLong();

	protected void beforeExecute(Thread t, Runnable r) {
		super.beforeExecute(t, r);
		log.fine(String.format("Thread %s: start %s", t, r));
		startTime.set(System.nanoTime());
	}

	protected void afterExecute(Runnable r, Throwable t) {
		try {
			long endTime = System.nanoTime();
			long taskTime = endTime - startTime.get();
			numTasks.incrementAndGet();
			totalTime.addAndGet(taskTime);
			log.fine(String.format("Thread %s: end %s, time=%dns", t, r, taskTime));
		} finally {
			super.afterExecute(r, t);
		}
	}

	protected void terminated() {
		try {
			log.info(String.format("Terminated: avg time=%dns", totalTime.get() / numTasks.get()));
		} finally {
			super.terminated();
		}
	}
}
