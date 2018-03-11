package com.yqc.beforepractice.demo;

import java.util.myconcurrent.locks.ReentrantReadWriteLock;

/**
 * 读写锁
 * 2015.10.27
 *
 * @author Yangqc
 */
public class ReadWriteLockTest {
    public static void main(String[] args) {
        final Queue3 queue = new Queue3();
        for (int i = 0; i < 10; i++) {
            new Thread(() -> queue.get()).start();

            new Thread(() -> queue.get()).start();

            new Thread(() -> queue.put("期成!")).start();
        }
    }
}

//只有一个线程写数据，但是可以有多个线程读数据
class Queue3 {
    //这里不能用Lock
    private Object data = null;
    private ReentrantReadWriteLock rw1 = new ReentrantReadWriteLock();

    public void get() {
        rw1.readLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to read data!");
            Thread.sleep((long) Math.random() * 1000);
            System.out.println(Thread.currentThread().getName() + " have read data:" + data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rw1.readLock().unlock();
        }
    }

    public void put(Object data) {
        rw1.writeLock().lock();
        try {
            System.out.println(Thread.currentThread().getName() + " be ready to write data!");
            Thread.sleep((long) Math.random() * 1000);
            this.data = data;
            System.out.println(Thread.currentThread().getName() + " have write data: " + data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rw1.writeLock().unlock();
        }
    }
}