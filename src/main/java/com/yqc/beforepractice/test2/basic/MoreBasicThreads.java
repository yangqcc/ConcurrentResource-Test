package com.yqc.beforepractice.test2.basic;

/**
 * 线程调度机制是非确定性的
 *
 * @author yangqc
 */
public class MoreBasicThreads {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++)
            new Thread(new LiftOff()).start();
        System.out.println("Waiting for LiftOff!");
    }
}
