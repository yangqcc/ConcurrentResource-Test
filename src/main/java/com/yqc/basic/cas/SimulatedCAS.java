package com.yqc.basic.cas;

public class SimulatedCAS {
    private int value;

    public synchronized int get() {
        return value;
    }

    public synchronized int compareAndSwap(int exceptedValue, int newValue) {
        int oldValue = value;
        if (oldValue == exceptedValue) {
            value = newValue;
        }
        return oldValue;
    }

    public synchronized boolean compareAndSet(int exceptedValue, int newValue) {
        return (exceptedValue == compareAndSwap(exceptedValue, newValue));
    }
}
