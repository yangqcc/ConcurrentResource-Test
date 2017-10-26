package com.yqc.beforePractice.test2.lockTest;


import com.yqc.beforePractice.test2.getResource.EventChecker;
import com.yqc.beforePractice.test2.getResource.IntGenerator;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MutextEvenGenerator extends IntGenerator {
    private int currentEvenValue = 0;
    private Lock lock = new ReentrantLock();

    public static void main(String[] args) {
        EventChecker.test(new MutextEvenGenerator());
    }

    @Override
    public int next() {
        lock.lock();
        try {
            ++currentEvenValue;
//			Thread.yield();
            return currentEvenValue;
        } finally {
            lock.unlock();
        }
    }
}
