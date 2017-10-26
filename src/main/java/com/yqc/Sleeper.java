package com.yqc;

public class Sleeper extends Thread {
    private int duration;

    public Sleeper(String name, int sleepTime) {
        super(name);
        duration = sleepTime;
        start();
    }

    public static void main(String[] args) {
        Sleeper sleepy = new Sleeper("Spleepy", 1500), grumpy = new Sleeper("Grumpy", 1500);
        Joiner dopey = new Joiner("Dopey", sleepy), doc = new Joiner("Doc", grumpy);
        grumpy.interrupt(); //睡眠被中断
    }

    public void run() {
        try {
            sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(getName() + " has awakened");
    }
}

class Joiner extends Thread {
    private Sleeper sleeper;

    public Joiner(String name, Sleeper sleeper) {
        super(name);
        this.sleeper = sleeper;
        start();
    }

    public void run() {
        try {
            sleeper.join(10); //如果10毫秒内，sleeper线程没有结束，join方法也总能返回
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(getName() + " join Completed");
    }
}
