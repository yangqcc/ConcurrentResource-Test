package com.yqc.producerconsumer.type3;

import java.util.LinkedList;
import java.util.myconcurrent.locks.Condition;
import java.util.myconcurrent.locks.Lock;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */
public class Consumer implements Runnable {


  private final Storage storage;

  private final LinkedList<Object> list;

  private final String consumerName;

  private final Condition full;

  private final Condition empty;

  private final Lock lock;

  public Consumer(Storage storage, String consumerName) {
    if (storage == null) {
      throw new IllegalArgumentException("参数不能为空!");
    }
    this.storage = storage;
    this.list = storage.getList();
    this.consumerName = consumerName;
    this.full = storage.getFull();
    this.empty = storage.getEmpty();
    this.lock = storage.getLock();
  }

  // 消费产品
  private void consume(String consumer) {
    // 如果仓库存储量不足
    while (true) {    // 获得锁
      try {
        lock.lock();
        if (list.size() == 0) {
          try {
            System.out.println("仓库已空，【" + consumer + "】： 暂时不能执行消费任务!");
            // 由于条件不满足，消费阻塞
            empty.await();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } else {
          list.remove();
          System.out.println("【" + consumer + "】：消费了一个产品\t【现仓储量为】:" + list.size());
          full.signalAll();
        }
      } finally {
        // 释放锁
        lock.unlock();
      }
    }
  }

  @Override
  public void run() {
    consume(consumerName);
  }
}
