package com.yqc.basic.cas;

/**
 * 基于CAS实现的非阻塞计数器
 *
 * @author yangqc
 * 2016年9月10日
 */
public class CasCounter {
    
    private SimulatedCAS value;

    public int getValue() {
        return value.get();
    }

    public int increment() {
        int v;
        do {
            v = value.get();
        } while (v != value.compareAndSwap(v, v + 1));
        return v + 1;
    }
}
