package com.yqc.sisuo;

/**
 * 如果一根筷子被使用，则获取该筷子的线程死锁，如果筷子drop，则唤醒其他线程
 *
 * @author yangqc 2016年7月26日
 */
public class Chopstick {
    private boolean taken = false;

    public synchronized void take() throws InterruptedException {
        while (taken) { // 如果这根筷子已经被拿起，则挂起当前请求的线程
            wait();
        }
        taken = true;
    }

    public synchronized void drop() {
        taken = false; // 当前筷子使用完毕，则唤醒其他等待的线程
        notifyAll();
    }
}
