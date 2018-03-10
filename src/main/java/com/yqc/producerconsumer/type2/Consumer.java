package com.yqc.producerconsumer.type2;

import java.util.LinkedList;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */
public class Consumer implements Runnable {

  private String consumerName;
  private Storage storage;
  private final LinkedList<Object> list;

  public Consumer(Storage storage, String consumerName) {
    this.storage = storage;
    this.consumerName = consumerName;
    if ((list = storage.getList()) == null) {
      throw new IllegalArgumentException("list不能为空!");
    }
  }

  @Override
  public void run() {
    consume(consumerName);
  }

  private void consume(String consumerName) {
    synchronized (list) {
      while (true) {
        //如果仓库存储量不足
        if (list.size() == 0) {
          try {
            System.out.println("仓库已空，【" + consumerName + "】： 暂时不能执行消费任务!");
            // 由于条件不满足，消费阻塞
            list.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } else {
          //对于LinkedList来说,查询并且返回,删除第一个元素
          //从头去元素,对于生产者来说,从尾部放入元素
          list.remove();
          System.out.println("【" + consumerName + "】：消费了一个产品\t【现仓储量为】:" + list.size());
          list.notifyAll();
        }
      }
    }
  }
}