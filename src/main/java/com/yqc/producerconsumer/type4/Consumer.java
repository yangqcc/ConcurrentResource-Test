package com.yqc.producerconsumer.type4;

import java.util.Queue;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */
public class Consumer implements Runnable {

  private final Queue<Object> queue;

  private final int MAX_NUM;

  public Consumer(Storage storage) {
    this.queue = storage.getQueue();
    this.MAX_NUM = storage.getMAX_NUM();
  }

  @Override
  public void run() {
    consume();
  }

  private void consume() {
    while (true) {
      if (queue.size() == 0) {
        System.out.println("仓库个数为0!");
        continue;
      }
      queue.poll();
      System.out.println("消费!");
    }
  }
}
