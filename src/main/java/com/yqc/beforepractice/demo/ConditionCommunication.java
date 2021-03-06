package com.yqc.beforepractice.demo;

import java.util.myconcurrent.locks.Condition;
import java.util.myconcurrent.locks.Lock;
import java.util.myconcurrent.locks.ReentrantLock;


public class ConditionCommunication {
    public static void main(String[] args) {
        final Business business = new Business();
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                business.sub(i);
            }
        }).start();
        for (int i = 0; i < 50; i++) {
            business.main(i);
        }
    }

    static class Business {
        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        private boolean bShouldSub = true;

        public void sub(int num) {
            lock.lock();
            try {
                while (bShouldSub) {
                    try {
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < 10; i++) {
                    System.out.println("sub thread sequence if " + i + " " + num);
                }
                bShouldSub = false;
//				this.notify();
                condition.signal();
            } finally {
                lock.unlock();
            }
        }

        public void main(int num) {
            lock.lock();
            try {
                while (!bShouldSub) {
                    try {
//						this.wait();
                        condition.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < 100; i++) {
                    System.out.println("main thread sequence if " + i + " "
                            + num);
                }
                bShouldSub = true;
//				this.notify();
                condition.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}
