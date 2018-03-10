package com.yqc.notify;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-09
 * @modified By yangqc
 */
public class WaitNotifyTest {

  private String[] shareObj = {"true"};

  public static void main(String[] args) {
    WaitNotifyTest test = new WaitNotifyTest();
    ThreadWait threadWait1 = test.new ThreadWait("wait thread1");
    threadWait1.setPriority(2);
    ThreadWait threadWait2 = test.new ThreadWait("wait thread2");
    threadWait2.setPriority(3);
    ThreadWait threadWait3 = test.new ThreadWait("wait thread3");
    threadWait3.setPriority(4);

    ThreadNotify threadNotify = test.new ThreadNotify("notify thread");

    threadNotify.start();
    threadWait3.start();
    threadWait2.start();
    threadWait1.start();
  }

  class ThreadWait extends Thread {

    public ThreadWait(String name) {
      super(name);
    }

    public void run() {
      synchronized (shareObj) {
        while ("true".equals(shareObj[0])) {
          System.out.println("线程" + this.getName() + "开始等待!");
          long startTime = System.currentTimeMillis();
          try {
            shareObj.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          long endTime = System.currentTimeMillis();
          System.out.println("线程" + this.getName() + "等待时间为:" + (endTime - startTime));
        }
      }
      System.out.println("线程" + getName() + "等待结束!");
    }
  }

  class ThreadNotify extends Thread {

    public ThreadNotify(String name) {
      super(name);
    }

    public void run() {
      try {
        sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      synchronized (shareObj) {
        System.out.println("线程" + this.getName() + "开始准备通知");
        shareObj[0] = "false";
        shareObj.notifyAll();
        System.out.println("线程" + this.getName() + "通知结束");
      }
      System.out.println("线程" + this.getName() + "运行结束");
    }
  }
}
