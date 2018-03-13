package com.yqc.looksupport;

import java.util.concurrent.locks.LockSupport;

/**
 * <p>title:</p>
 * <p>description:LockSupport被中断后，会立即唤醒当前线程并返回</p>
 *
 * @author yangqc
 * @date Created in 2018-03-13
 * @modified By yangqc
 */
public class LockSupportInterrupt {

  public static void main(String[] args) throws InterruptedException {
    Runnable myTask = new MyTask();
    Thread thread = new Thread(myTask);
    thread.start();
    Thread.sleep(2000);
    thread.interrupt();
  }
}

class MyTask implements Runnable {

  @Override
  public void run() {
    while (true) {
      LockSupport.park();
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
    }
    System.out.println(Thread.currentThread().isInterrupted());
    System.out.println("被中断了!");
  }
}

class MyTask2 implements Runnable {

  @Override
  public void run() {
    while (true) {
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
    }
    System.out.println(Thread.currentThread().isInterrupted());
    System.out.println("被中断了!");
  }
}
