package com.yqc.forkjoin.cancel;

import java.util.concurrent.ForkJoinPool;

/**
 * @author yangqc
 */
public class Main {

  public static void main(String[] args) {
    ArrayGenerator arrayGenerator = new ArrayGenerator();
    TaskManager taskManager = new TaskManager();
    int[] arrays = arrayGenerator.generator(10000);
    SearchNumberTask searchNumberTask = new SearchNumberTask(arrays, 0, arrays.length, 8,
        taskManager);
    ForkJoinPool pool = new ForkJoinPool();
    pool.execute(searchNumberTask);
    System.out.println(searchNumberTask.join());
  }
}
