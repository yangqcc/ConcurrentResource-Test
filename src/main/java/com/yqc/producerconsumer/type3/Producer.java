package com.yqc.producerconsumer.type3;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */
public class Producer implements Runnable {

  private final Storage storage;

  private final LinkedList<Object> list;

  private final int MAX_SIZE;

  private final String producerName;

  private final Condition full;

  private final Condition empty;

  private final Lock lock;

  public Producer(Storage storage, String producerName) {
    if (storage == null) {
      throw new IllegalArgumentException("参数不能为空!");
    }
    this.storage = storage;
    this.list = storage.getList();
    this.MAX_SIZE = storage.getMAX_SIZE();
    this.producerName = producerName;
    this.full = storage.getFull();
    this.empty = storage.getEmpty();
    this.lock = storage.getLock();
  }

  @Override
  public void run() {
    produce();
  }

  private void produce() {
    while (true) {
      try {
        lock.lock();
        // 如果仓库已满
        if (list.size() == MAX_SIZE) {
          try {
            System.out.println("仓库已满，【" + producerName + "】： 暂时不能执行生产任务!");
            // 由于条件不满足，生产阻塞
            full.await();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } else {
          // 生产产品
          list.add(new Object());
          System.out.println("【" + producerName + "】：生产了一个产品\t【现仓储量为】:" + list.size());
          empty.signalAll();
        }
      } finally {
        // 释放锁
        lock.unlock();
      }
    }
  }
}
