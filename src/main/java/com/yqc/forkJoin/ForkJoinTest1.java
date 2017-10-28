package com.yqc.forkJoin;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * forkjoin执行任务
 * Created by yangqc on 2017/10/28
 */
public class ForkJoinTest1 {

    private static class Task extends RecursiveTask<Integer> {

        private Integer[] values;
        private Integer sum = 0;
        private Integer threshold;

        Task(Integer[] values, Integer threshold) {
            if (values == null || values.length == 0) {
                throw new IllegalArgumentException("数组长度不能为空或者为0!");
            }
            this.values = values;
            this.threshold = threshold;
        }

        /**
         * 计算
         *
         * @return
         */
        protected Integer computeDirectly() {
            System.out.println(Thread.currentThread().getName());
            for (int i = 0; i < values.length; i++) {
                sum += values[i];
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return sum;
        }

        @Override
        protected Integer compute() {
            int length = values.length;
            if (length <= threshold) {
                computeDirectly();
                return sum;
            }
            int split = length / 2;
            Task leftTask = new Task(Arrays.copyOfRange(values, 0, split), threshold);
            Task rightTask = new Task(Arrays.copyOfRange(values, split + 1, values.length), threshold);
            // 执行子任务
            leftTask.fork();
            rightTask.fork();
            // 等待子任务执行完，并得到其结果
            int leftResult = leftTask.join();
            int rightResult = rightTask.join();
            // 合并子任务
            sum = leftResult + rightResult;
            return sum;
        }
    }

    public static void main(String[] args) {
        Integer[] values = {1, 2, 4, 5, 3242, 2, 32, 3, 3, 3, 3, 334, 89, 45, 354, 90, 5423, 54, 32, 543, 2};
        Task task = new Task(values, 2);
        ForkJoinPool pool = new ForkJoinPool(4);
        Future<Integer> result = pool.submit(task);
        try {
            System.out.println(result.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}

