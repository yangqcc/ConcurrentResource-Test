package com.yqc.beforepractice.test2.lockTest;


import com.yqc.beforepractice.test2.getResource.EventChecker;
import com.yqc.beforepractice.test2.getResource.IntGenerator;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicEvenGenerator extends IntGenerator {

    private AtomicInteger currentEvenValue = new AtomicInteger(0);

    public static void main(String[] args) {
        EventChecker.test(new AtomicEvenGenerator());
    }

    @Override
    public int next() {
        return currentEvenValue.addAndGet(2);
    }
}
