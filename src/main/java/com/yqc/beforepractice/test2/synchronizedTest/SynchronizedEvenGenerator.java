package com.yqc.beforepractice.test2.synchronizedTest;


import com.yqc.beforepractice.test2.getResource.EventChecker;
import com.yqc.beforepractice.test2.getResource.IntGenerator;

public class SynchronizedEvenGenerator extends IntGenerator {

    private int currentEvenValue = 0;

    public static void main(String[] args) {
        EventChecker.test(new SynchronizedEvenGenerator());
    }

    @Override
    public synchronized int next() {
        ++currentEvenValue;
        Thread.yield();
        ++currentEvenValue;
        return currentEvenValue;
    }
}
