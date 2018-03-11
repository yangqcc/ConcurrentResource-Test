package com.yqc.basic.stop;

import java.util.concurrent.TimeUnit;
import java.util.myconcurrent.locks.Lock;
import java.util.myconcurrent.locks.ReentrantLock;

class BlockedMutex {
    private Lock lock = new ReentrantLock(); // ReentrantLock上阻塞的任务具备可以被中断的能力

    public BlockedMutex() {
        lock.lock(); // 构造器获取锁
    }

    public void f() { // f()可以打断被阻塞的互斥调用
        try {
            lock.lockInterruptibly(); //
            System.out.println("lock acquired in f()");
        } catch (InterruptedException e) {
            System.out.println("Interrupted form lock acquisiton in f()");
        }
    }
}

class Blocked2 implements Runnable {

    BlockedMutex blocked = new BlockedMutex();

    @Override
    public void run() {
        System.out.println("Waiting for f() in BlockedMutex");
        blocked.f(); // 调用f()被阻塞
        System.out.println("Broken out of blocked call");
    }
}

public class Interrupting2 {
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(new Blocked2());
        t.start();
        TimeUnit.SECONDS.sleep(1);
        System.out.println("Issuing t.interrupt()");
        t.interrupt();
    }
}
