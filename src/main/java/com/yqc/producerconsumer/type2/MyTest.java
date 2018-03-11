package com.yqc.producerconsumer.type2;

import util.concurrent.Executor;
import util.concurrent.Executors;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */
public class MyTest {

  public static void main(String[] args) {
    Executor executor = Executors.newCachedThreadPool();
    Storage storage = new Storage();
    Producer producer = new Producer(storage, "produce1");
    Producer producer2 = new Producer(storage, "produce2");
    Consumer consumer = new Consumer(storage, "consumer1");
    Consumer consumer2 = new Consumer(storage, "consumer2");
    Consumer consumer3 = new Consumer(storage, "consumer3");
    executor.execute(producer);
    executor.execute(producer2);
    executor.execute(consumer);
    executor.execute(consumer2);
    executor.execute(consumer3);
  }
}
