package com.yqc.resource;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MutexEvenGenerator extends IntGenerator {
    private int currentEvenValue = 0;
    private Lock lock = new ReentrantLock();

    public static void main(String[] args) {
        EvenChecker.test(new MutexEvenGenerator());
    }

    @Override
    public int next() {
        lock.lock();
        try {
            ++currentEvenValue;
            ++currentEvenValue;
            return currentEvenValue;  //return语句放在lock里面
        } finally {
            lock.unlock();
        }
    }
}
