package com.yqc.execute;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/**
 * 
 *
 * @author yangqc
 * 2016年8月1日
 */
public class TestFuture implements Callable<Integer> {

	private volatile int a = 2;

	@Override
	public Integer call() throws Exception {
		while (a < 100000) {
			TimeUnit.SECONDS.sleep(1);
			a++;
			System.out.println("haha!"+a);
		}
		return 2;
	}

	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		Future<Integer> future = exec.submit(new TestFuture());
		// exec.shutdown();
		try {
			System.out.println(future.get(4, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			// e.printStackTrace();
			future.cancel(true);   //如果不取消，即使抛出TimeoutException异常，线程还是会继续执行
			System.out.println(future.isCancelled());
		} // 如果在指定时间内没有获取到结果，那么future会抛出异常
	}
}
