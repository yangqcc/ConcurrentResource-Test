package com.yqc.producerconsumer.type3;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

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
    Consumer consumer1 = new Consumer(storage, "consumer1");
    Consumer consumer2 = new Consumer(storage, "consumer2");
    Consumer consumer3 = new Consumer(storage, "consumer3");

    Producer producer1 = new Producer(storage, "produce1");
    Producer producer2 = new Producer(storage, "produce2");
    Producer producer3 = new Producer(storage, "produce3");
    Producer producer4 = new Producer(storage, "produce4");

    Executor executor = Executors.newCachedThreadPool();
    executor.execute(consumer1);
    executor.execute(consumer2);
    executor.execute(consumer3);
    executor.execute(producer1);
    executor.execute(producer2);
    executor.execute(producer3);
    executor.execute(producer4);
  }
}
