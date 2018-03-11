package com.yqc.beforepractice.demo;

import java.util.myconcurrent.locks.Condition;
import java.util.myconcurrent.locks.Lock;
import java.util.myconcurrent.locks.ReentrantLock;

public class ThreeConditionCommunication {
    public static void main(String[] args) {
        final Business business = new Business();
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                business.sub1(i);
            }
        }).start();

        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                business.sub2(i);
            }
        }).start();

        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                business.sub3(i);
            }
        }).start();
    }

    static class Business {
        Lock lock = new ReentrantLock();
        Condition condition1 = lock.newCondition();
        Condition condition2 = lock.newCondition();
        Condition condition3 = lock.newCondition();
        private int bShouldSub = 1;

        public void sub1(int num) {
            lock.lock();
            try {
                while (bShouldSub != 1) {
                    try {
                        condition1.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < 10; i++) {
                    System.out.println("sub1 thread sequence if " + i + " " + num);
                }
                bShouldSub = 2;
                condition2.signal();
            } finally {
                lock.unlock();
            }
        }

        public void sub2(int num) {
            lock.lock();
            try {
                while (bShouldSub != 2) {
                    try {
                        condition2.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < 100; i++) {
                    System.out.println("sub2 thread sequence if " + i + " "
                            + num);
                }
                bShouldSub = 3;
                condition3.signal();
            } finally {
                lock.unlock();
            }
        }

        public void sub3(int num) {
            lock.lock();
            try {
                while (bShouldSub != 3) {
                    try {
                        condition3.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < 100; i++) {
                    System.out.println("sub3 thread sequence if " + i + " "
                            + num);
                }
                bShouldSub = 1;
                condition1.signal();
            } finally {
                lock.unlock();
            }
        }
    }
}
