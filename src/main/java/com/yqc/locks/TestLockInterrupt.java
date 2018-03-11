package com.yqc.locks;

import java.util.myconcurrent.locks.Lock;
import java.util.myconcurrent.locks.ReentrantLock;

public class TestLockInterrupt {
    private Lock lock = new ReentrantLock();

    public static void main(String[] args) {
        TestLockInterrupt test = new TestLockInterrupt();
        MyThread thread1 = new MyThread(test);
        MyThread thread2 = new MyThread(test);
        thread1.start();
        thread2.start();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread2.interrupt();
    }

    public void insert(Thread thread) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            System.out.println(thread.getName() + "获取了锁!");
            long startTime = System.currentTimeMillis();
            for (; ; ) {
                if (System.currentTimeMillis() - startTime >= 10000) {
                    break;
                }
            }
        } finally {
            System.out.println(Thread.currentThread().getName() + "执行了finally!");
            lock.unlock();
            System.out.println(thread.getName() + "释放了锁!");
        }
    }
}

class MyThread extends Thread {
    private TestLockInterrupt test = null;

    public MyThread(TestLockInterrupt test) {
        this.test = test;
    }

    @Override
    public void run() {
        try {
            test.insert(Thread.currentThread());
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + "被中断!");
        }
    }
}