package com.yqc.resource;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicEvenGenerator extends IntGenerator {

    private AtomicInteger currentEvenValue = new AtomicInteger(0);

    public static void main(String[] args) {
        EvenChecker.test(new AtomicEvenGenerator());
    }

    @Override
    public int next() {
        return currentEvenValue.addAndGet(2);
    }
}
