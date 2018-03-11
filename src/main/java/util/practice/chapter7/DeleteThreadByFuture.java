package util.practice.chapter7;

import util.concurrent.Executors;
import java.util.concurrent.*;
import util.concurrent.Future;

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
