package java.util.practice.chapter8;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ���߳��ύ���̳߳ص�������࣬�̳߳����˹������̻߳�ִ�н�Ҫ�ύ���̳߳ص�����
 *
 * @author yangqc 2016��8��13��
 */
public class TestBoundedExecutor {
	public static void main(String[] args) {
		AtomicInteger count = new AtomicInteger(0);
		ThreadPoolExecutor exec = new ThreadPoolExecutor(2, 4, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2));
		exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				System.out.println(
						"hello,this is runnable! " + Thread.currentThread().getName() + " " + count.getAndIncrement());
			}
		};
		for (int i = 0; i < 100; i++) {
			exec.execute(runnable);
		}
		exec.shutdown();
		System.out.println("this is main Thread!");
	}
}
