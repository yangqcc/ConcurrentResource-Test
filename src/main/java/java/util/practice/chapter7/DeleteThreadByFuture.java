package java.util.practice.chapter7;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DeleteThreadByFuture {
	public static void timedRun(Runnable r, long timeout, TimeUnit unit) {
		ExecutorService exec = Executors.newCachedThreadPool();
		Future<?> task = exec.submit(r);
		try {
			task.get(timeout, unit);
		} catch (TimeoutException e) {
			// ����ȡ��
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			task.cancel(true); // ���������Ȼ�����У���ô�����ж�
		}
		exec.shutdown();
	}
}
