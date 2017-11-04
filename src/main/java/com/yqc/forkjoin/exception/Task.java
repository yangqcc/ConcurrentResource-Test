package com.yqc.forkjoin.exception;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * @author yangqc
 */
public class Task extends RecursiveTask<Integer> {

  private static final long serialVersionUID = 1L;
  private int[] array;
  private int start, end;

  public Task(int[] array, int start, int end) {
    this.array = array;
    this.start = start;
    this.end = end;
  }

  @Override
  protected Integer compute() {
    System.out.printf("Task:Start from %d to %d\n", start, end);
    if (end - start < 10) {
      if (start < 3 && end > 3) {
        throw new RuntimeException(
            "This task throws an Exception: Task from " + start + " to " + end);
      }
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } else {
      int mid = (start + end) / 2;
      Task task1 = new Task(array, start, mid);
      Task task2 = new Task(array, mid, end);
      invokeAll(task1, task2);
    }
    System.out.printf("Task:End from %d to %d\n", start, end);
    return 0;
  }
}

class TaskMain {

  public static void main(String[] args) {
    ForkJoinPool pool = new ForkJoinPool();
    int[] array = new int[100];
    Task task = new Task(array, 0, 100);
    pool.execute(task);
    pool.shutdown();
    try {
      pool.awaitTermination(1, TimeUnit.DAYS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //如果任务没有正常结束
    if (task.isCompletedAbnormally()) {
      System.out.println("Main: An exception has occurred!");
      System.out.println("Main: " + task.getException());
    }
    System.out.println("Main: Result: " + task.join());
  }
}
