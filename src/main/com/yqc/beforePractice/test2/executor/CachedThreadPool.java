package com.yqc.beforePractice.test2.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.yqc.beforePractice.test2.LiftOff;
import com.yqc.basic.execute.executor.LiftOff;

/**
 * 单个Executors来管理所有线程任务 线程池
 * 
 * @author yangqc
 * 
 */
public class CachedThreadPool {
	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		for (int i = 0; i < 5; i++)
			exec.execute(new LiftOff());
		exec.shutdown();
	}
}
