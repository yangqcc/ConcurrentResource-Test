package com.yqc.beforePractice.test2.priority;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 线程优先级
 * java中的优先级与操作系统中的优先级一般来说不是一一对应
 * 最好使用Thread.MAX_PRIORITY NORM_PRIORITY MIN_PRIORITY
 *
 * @author yangqc
 */
public class SimplePriorities implements Runnable {
    private int countDown = 5;
    private volatile double d;
    private int priority;

    public SimplePriorities(int priority) {
        this.priority = priority;
    }

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 5; i++)
            exec.execute(new SimplePriorities(Thread.MIN_PRIORITY));
        exec.execute(new SimplePriorities(Thread.MAX_PRIORITY));
        exec.shutdown();
    }

    public String toString() {
        return Thread.currentThread() + ":" + countDown;
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(priority);  //设置优先级
        while (true) {
            for (int i = 1; i < 10000000; i++) {
                d += (Math.PI + Math.E) / (double) i;
                if (i % 1000 == 0)
                    Thread.yield(); //让步  重要的控制最好不要依赖于yield
            }
            System.out.println(this);
            if (--countDown == 0)
                return;
        }
    }
}
