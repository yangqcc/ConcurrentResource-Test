package com.yqc.beforePractice.thinkinjava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 2016.1.4
 *
 * @author yangqc
 */
public class AtomicityTest implements Runnable {
    private int i = 0;

    public static void main(String[] args) throws InterruptedException {
        ExecutorService exec = Executors.newCachedThreadPool();
        AtomicityTest at = new AtomicityTest();
        exec.execute(at);
        while (true) {
            int val = at.getValue();
            System.out.println(val);
            if (val % 2 != 0) {
                System.out.println(val);
                System.exit(0);
            }
        }
    }

    public int getValue() {
        return i;
    }

    private synchronized void evenIncrement() throws InterruptedException {
        i++;
        Thread.sleep(10);
        i++;
    }

    @Override
    public void run() {
        while (true) {
            try {
                evenIncrement();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
