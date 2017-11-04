package com.yqc.forkjoin.cancel;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * 查找数字任务类
 *
 * @author yangqc
 */
public class SearchNumberTask extends RecursiveTask<Integer> {

  private static final long serialVersionUID = 1L;
  private int[] array;
  private int start, end;
  private int number;
  private TaskManager taskManager;
  private final int NOT_FOUND = -1;
  private final int threshold = 10;

  /**
   * @param array 需要查找数字的数组
   * @param start 查找开始位置
   * @param end 查找结束位置
   * @param targetNumber 查找目标数字
   * @param taskManager 任务管理器
   */
  public SearchNumberTask(int[] array, int start, int end, int targetNumber,
      TaskManager taskManager) {
    this.array = array;
    this.start = start;
    this.end = end;
    this.number = targetNumber;
    this.taskManager = taskManager;
  }

  @Override
  protected Integer compute() {
    System.out.println("Task: " + start + ":" + end);
    int ret;
    if (end - start > threshold) {
      ret = launchTasks();
    } else {
      ret = lookForNumber();
    }
    return ret;
  }

  /**
   * 重新划分任务
   *
   * @return 返回子任务查找到数字的位置
   */
  private int launchTasks() {
    int mid = (start + end) / 2;
    SearchNumberTask task1 = new SearchNumberTask(array, start, mid, number, taskManager);
    SearchNumberTask task2 = new SearchNumberTask(array, mid + 1, end, number, taskManager);
    taskManager.addTask(task1);
    taskManager.addTask(task2);
    //采用异步的方式
    task1.fork();
    task2.fork();
    int ret = 0;
    try {
      if (!task1.isCancelled()) {
        ret = task1.join();
        if (ret != -1) {
          return ret;
        }
      }
    } catch (Exception e) {
      ret = -1;
    }
    try {
      if (!task2.isCancelled()) {
        ret = task2.join();
      }
    } catch (Exception e) {
      ret = -1;
    }
    return ret;
  }

  /**
   * 查找数字
   *
   * @return 返回查找数字的位置
   */
  private int lookForNumber() {
    try {
      for (int i = start; i < end; i++) {
        if (array[i] == number) {
          System.out
              .printf("SearchNumberTask: Number %d found in position %d and Thread name is %s\n",
                  number, i, Thread.currentThread().getName());
          taskManager.cancelTasks(this);
          return i;
        }
        TimeUnit.MILLISECONDS.sleep(1);
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return NOT_FOUND;
  }

  public void writeCancelMessage() {
    System.out.printf("Task: Cancelled task from %d to %d\n", start, end);
  }
}
