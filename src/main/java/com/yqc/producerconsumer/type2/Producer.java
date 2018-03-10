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
public class Producer extends Thread {

  private final String producerName;
  private final Storage storage;
  private final LinkedList<Object> list;
  private final int MAX_SIZE;

  public Producer(Storage storage, String producerName) {
    this.storage = storage;
    this.producerName = producerName;
    if ((this.list = storage.getList()) == null) {
      throw new IllegalArgumentException("list不能为空!");
    }
    if ((MAX_SIZE = storage.getMAX_SIZE()) <= 0) {
      throw new IllegalArgumentException("MAX_SIZE不能小于0!");
    }
  }

  @Override
  public void run() {
    produce(producerName);
  }

  // 生产产品
  public void produce(String producer) {
    synchronized (list) {
      while (true) {
        // 如果仓库已满
        if (list.size() == MAX_SIZE) {
          try {
            System.out.println("仓库已满，【" + producer + "】： 暂时不能执行生产任务!");
            // 由于条件不满足，生产阻塞
            list.wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        } else {// 生产产品
          list.add(new Object());
          System.out.println("【" + producer + "】：生产了一个产品\t【现仓储量为】:" + list.size());
          list.notifyAll();
        }
      }
    }
  }
}
