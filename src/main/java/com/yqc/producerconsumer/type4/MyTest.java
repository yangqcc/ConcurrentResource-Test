package com.yqc.producerconsumer.type4;

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
    Storage storage = new Storage();
    Executor executor = Executors.newCachedThreadPool();

    Consumer consumer1 = new Consumer(storage);
    Consumer consumer2 = new Consumer(storage);
    Consumer consumer3 = new Consumer(storage);

    Producer producer1 = new Producer(storage);
    Producer producer2 = new Producer(storage);
    Producer producer3 = new Producer(storage);
    executor.execute(consumer1);
    executor.execute(consumer2);
    executor.execute(consumer3);
    executor.execute(producer1);
    executor.execute(producer2);
    executor.execute(producer3);
  }
}
