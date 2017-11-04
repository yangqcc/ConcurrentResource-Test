package com.yqc.forkjoin.saleproduct;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by yangqc on 2017/10/28
 */
public class Main {
    public static void main(String[] args) {
        ProductListGenerator generator = new ProductListGenerator();
        List<Product> productList = generator.generate(10000);
        Task task = new Task(productList, 0, productList.size(), 0.2);
        ForkJoinPool pool = new ForkJoinPool();
        pool.execute(task);
        while (!task.isDone()) {
            System.out.printf("Main:Thread Count:%d\n", pool.getActiveThreadCount());
            System.out.printf("Main:Thread Steal；%d\n", pool.getStealCount());
            System.out.printf("Main:Parallelism:%d\n", pool.getParallelism());
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        pool.shutdown();
        if (task.isCompletedNormally()) {
            System.out.println("Main:The process has completed normally.");
        }
        productList.stream().forEach(s -> System.out.println(s.getPrice()));
    }
}
