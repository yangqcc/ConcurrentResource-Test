package com.yqc.basic.execute;

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
 * 2016��8��1��
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
			future.cancel(true);   //�����ȡ������ʹ�׳�TimeoutException�쳣���̻߳��ǻ����ִ��
			System.out.println(future.isCancelled());
		} // �����ָ��ʱ����û�л�ȡ���������ôfuture���׳��쳣
	}
}
