package com.yqc.callable;

import java.util.concurrent.Callable;

/**
 * @author yangqc
 * @date 2017/10/30
 */
public class CalculateTask implements Callable<Integer> {

    private int start;
    private int sum;
    private int end;

    public CalculateTask(int end) {
        start = 0;
        sum = 0;
        if (end < start) {
            throw new IllegalArgumentException("参数不能小于" + start);
        }
        this.end = end;
    }

    @Override
    public Integer call() {
        for (int i = start; i < end; i++) {
            sum += i;
            System.out.println("current thread name is" + Thread.currentThread().getName() + ", and current sum is " + sum);
        }
        return sum;
    }
}
