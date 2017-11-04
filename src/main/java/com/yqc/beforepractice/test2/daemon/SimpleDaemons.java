package com.yqc.beforepractice.test2.daemon;

public class SimpleDaemons implements Runnable {

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread daemon = new Thread(new SimpleDaemons());
            daemon.setDaemon(true);
            daemon.start();
        }
        System.out.println("All daemons started");
        Thread.sleep(1750);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
            while (true) {
                System.out.println(Thread.currentThread() + " " + this);
            }
        } catch (Exception e) {
            System.out.println("sleep() interrupted");
        }
    }

}
