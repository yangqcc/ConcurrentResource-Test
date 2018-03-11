package com.yqc.condition;


import util.concurrent.Executor;
import util.concurrent.Executors;
import util.concurrent.locks.Condition;
import util.concurrent.locks.Lock;
import util.concurrent.locks.ReentrantLock;

/**
 * <p>title:</p> <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2017-11-19
 * @modified By yangqc
 */
public class BoundedBuffer {

  private final Lock lock = new ReentrantLock();
  private final Condition notFull = lock.newCondition();
  private final Condition notEmpty = lock.newCondition();

  //缓存队列
  private final Object[] items;
  private int putPtr, takePtr, count;

  public BoundedBuffer(int count) {
    if (count <= 0) {
      throw new IllegalArgumentException("count不能小于等于0!");
    }
    items = new Object[count];
  }


  public void put(Object x) {
    try {
      lock.lock();
      while (count == items.length) {
        notFull.await(); //阻塞写线程
      }
      items[putPtr] = x;
      if (++putPtr == items.length) {
        putPtr = 0;
      }
      ++count;
      System.out.println(Thread.currentThread().getName() + "放入元素!");
      notEmpty.signal();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  public Object take() {
    try {
      Object object;
      lock.lock();
      while (count == 0) {
        notEmpty.await();
      }
      object = items[takePtr];
      items[takePtr] = null;
      if (++takePtr == items.length) {
        takePtr = 0;
      }
      count--;
      System.out.println(Thread.currentThread().getName() + "获取元素!");
      notFull.signal();
      return object;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }
}

class PutTask implements Runnable {

  private final BoundedBuffer boundedBuffer;

  PutTask(BoundedBuffer boundedBuffer) {
    this.boundedBuffer = boundedBuffer;
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(1000);
        boundedBuffer.put(new Object());
      } catch (Exception e) {
        System.out.println("出错了!");
        break;
      }
    }
  }
}

class TakeTask implements Runnable {

  private final BoundedBuffer boundedBuffer;

  TakeTask(BoundedBuffer boundedBuffer) {
    this.boundedBuffer = boundedBuffer;
  }

  @Override
  public void run() {
    while (true) {
      try {
        Thread.sleep(1000);
        boundedBuffer.take();
      } catch (InterruptedException e) {
        System.out.println("出错了!");
        break;
      }
    }
  }
}

class ExecuteClass {

  public static void main(String[] args) {
    BoundedBuffer boundedBuffer = new BoundedBuffer(100);
    Executor executor = Executors.newCachedThreadPool();
    for (int i = 0; i < 10; i++) {
      executor.execute(new PutTask(boundedBuffer));
    }
    for (int i = 0; i < 8; i++) {
      executor.execute(new TakeTask(boundedBuffer));
    }
  }
}
