package com.yqc.forkJoin.cancel;

import java.util.concurrent.ForkJoinPool;

/**
 * Created by yangqc on 2017/10/29
 */
public class Main {
    public static void main(String[] args) {
        ArrayGenerator arrayGenerator = new ArrayGenerator();
        TaskManager taskManager = new TaskManager();
        int[] arrays = arrayGenerator.generator(10000);
        SearchNumberTask searchNumberTask = new SearchNumberTask(arrays, 0, arrays.length, 8, taskManager);
        ForkJoinPool pool = new ForkJoinPool();
        pool.execute(searchNumberTask);
        System.out.println(searchNumberTask.join());
    }
}
