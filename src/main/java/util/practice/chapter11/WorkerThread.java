package util.practice.chapter11;

import util.concurrent.BlockingQueue;

/**
 * �����������ʱ���Ǵ��з���
 *
 * @author yangqc
 * 2016��8��16��
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
                break; // �����߳��˳�
            }
        }
    }
}
