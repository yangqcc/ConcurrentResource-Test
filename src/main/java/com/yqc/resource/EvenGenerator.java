package com.yqc.resource;

import java.util.concurrent.TimeUnit;

/**
 * java递增不是原子性的
 *
 * @author yangqc
 * 2016年7月21日
 */
public class EvenGenerator extends IntGenerator {
    private int currentEvenValue = 0;

    public static void main(String[] args) {
        EvenChecker.test(new EvenGenerator());
    }

    @Override
    public int next() {
        ++currentEvenValue;   //这里会出现错误
        Thread.yield();
        try {
            TimeUnit.MICROSECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ++currentEvenValue;
        return currentEvenValue;
    }

}
