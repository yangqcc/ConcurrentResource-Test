package com.yqc.notify;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-09
 * @modified By yangqc
 */
public class NotifyAndWaitTest1 {

  public static void main(String[] args) throws InterruptedException {
    Object monitorObject = new Object();
    NotifyTask notifyTask = new NotifyTask(monitorObject);
    WaitTask waitTask1 = new WaitTask(monitorObject);
    WaitTask waitTask2 = new WaitTask(monitorObject);
    Thread thread1 = new Thread(waitTask1);
    Thread thread2 = new Thread(waitTask2);
    thread1.setPriority(2);
    thread2.setPriority(1);
    thread1.start();
    Thread.sleep(1000);
    thread2.start();
    Thread.sleep(2000);
    new Thread(notifyTask).start();
    new Thread(notifyTask).start();
  }

  static class NotifyTask implements Runnable {

    private final Object monitorObject;

    public NotifyTask(Object monitorObject) {
      this.monitorObject = monitorObject;
    }

    @Override
    public void run() {
      synchronized (monitorObject) {
        monitorObject.notify();
      }
    }
  }

  static class WaitTask implements Runnable {

    private final Object monitorObject;

    public WaitTask(Object monitorObject) {
      this.monitorObject = monitorObject;
    }

    @Override
    public void run() {
      synchronized (monitorObject) {
        System.out.println(Thread.currentThread().getName());
        try {
          monitorObject.wait();
          System.out.println(Thread.currentThread().getName());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }

}

