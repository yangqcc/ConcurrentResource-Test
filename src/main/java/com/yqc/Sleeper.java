package com.yqc;

/**
 * join等待线程
 */
public class Sleeper extends Thread {

  private int duration;

  public Sleeper(String name, int sleepTime) {
    super(name);
    duration = sleepTime;
    start();
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
      //如果10毫秒内，sleeper线程没有结束，join方法也总能返回
      sleeper.join(10);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println(getName() + " join Completed");
  }
}

class Main {

  public static void main(String[] args) {
    Sleeper sleepy = new Sleeper("Sleepy", 15000), grumpy = new Sleeper("Grumpy", 15000);
    Joiner joiner1 = new Joiner("Joiner1", sleepy), joiner2 = new Joiner("Joiner2", grumpy);
    grumpy.interrupt(); //睡眠被中断
  }
}