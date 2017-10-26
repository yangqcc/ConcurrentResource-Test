package com.yqc.beforePractice.test2.basic;

/**
 * 线程中同时运行两个方法，main方法和LiftOff中的run方法 程序中“同时”执行的代码
 *
 * @author yangqc
 */
public class BadicThreads {
    public static void main(String[] args) {
        Thread t = new Thread(new LiftOff());
        t.start();
        System.out.println("Waiting for LiftOff!");
    }
}
