package com.yqc.basic.execute.executor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ���Ա��Ͳ��ԣ����̳߳����󣬲����н���б������󣬿�ʼִ�б��Ͳ��ԣ�setRejectedExecutionPoolʵ�֣�
 * Ĭ�����׳�δ����RejectedExecutionException 
 * (��ֹ���Ͳ���)
 * 
 * @author yangqc 2016��9��4��
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
