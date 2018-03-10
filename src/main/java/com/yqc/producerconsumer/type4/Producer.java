package com.yqc.producerconsumer.type4;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */
public class Producer implements Runnable {

  private final Queue<Object> queue;

  private final int MAX_NUM;

  public Producer(Storage storage) {
    this.queue = storage.getQueue();
    this.MAX_NUM = storage.getMAX_NUM();
  }

  @Override
  public void run() {
    while (true) {
      if (queue.size() == MAX_NUM) {
        System.out.println("仓库已满,不能执行存放任务!");
      }
      queue.offer(new Object());
      System.out.println("生产!");
    }
  }
}
