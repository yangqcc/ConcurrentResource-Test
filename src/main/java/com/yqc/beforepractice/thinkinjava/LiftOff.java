package com.yqc.beforepractice.thinkinjava;

public class LiftOff implements Runnable {
    private static int taskCount = 0;
    private final int id = taskCount++;
    protected int countDown = 10;

    public LiftOff() {
    }

    public LiftOff(int countDown) {
        this.countDown = countDown;
    }

    public String status() {
        return "#" + id + "(" + (countDown > 0 ? countDown : "Liftoff!") + "),";
    }

    @Override
    public void run() {
        while (countDown-- > 0) {
            System.out.println(status());
            Thread.yield();
        }
    }

}
