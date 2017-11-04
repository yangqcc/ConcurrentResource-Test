package com.yqc.forkjoin.cancel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * 任务管理类
 *
 * @author yangqc
 */
public class TaskManager {

  /**
   * 存放任务的列表
   */
  private List<ForkJoinTask<Integer>> tasks;

  public TaskManager() {
    tasks = new ArrayList<>();
  }

  /**
   * 添加任务
   *
   * @param task 将要添加的任务
   */
  public void addTask(ForkJoinTask<Integer> task) {
    tasks.add(task);
  }

  /**
   * 除此任务外，将要取消的任务
   *
   * @param cancelTask 将要保留的任务
   */
  public void cancelTasks(ForkJoinTask<Integer> cancelTask) {
    for (ForkJoinTask<Integer> task : tasks) {
      if (task != cancelTask) {
        task.cancel(true);
        ((SearchNumberTask) task).writeCancelMessage();
      }
    }
  }
}
