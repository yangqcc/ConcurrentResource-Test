package com.yqc.forkJoin.cancel;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangqc on 2017/10/29
 */
public class SearchNumberTask extends RecursiveTask<Integer> {

    private static final long serialVersionUID = 1L;
    private int array[];
    private int start, end;
    private int number;
    private TaskManager taskManager;
    private final static int NOT_FOUND = -1;

    private SearchNumberTask(int[] array, int start, int end, int number, TaskManager taskManager) {
        this.array = array;
        this.start = start;
        this.end = end;
        this.number = number;
        this.taskManager = taskManager;
    }

    @Override
    protected Integer compute() {
        System.out.println("Task: " + start + ":" + end);
        int ret;
        if (end - start > 10) {
            ret = launchTasks();
        } else {
            ret = lookForNumber();
        }
        return ret;
    }

    private int launchTasks() {
        int mid = (start + end) / 2;
        SearchNumberTask task1 = new SearchNumberTask(array, start, mid, number, taskManager);
        SearchNumberTask task2 = new SearchNumberTask(array, mid, end, number, taskManager);
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        //采用异步的方式
        task1.fork();
        task2.fork();
        int ret;
        ret = task1.join();
        if (ret != -1) {
            return ret;
        }
        ret = task2.join();
        return ret;
    }

    private int lookForNumber() {
        try {
            for (int i = start; i < end; i++) {
                if (array[i] == number) {
                    System.out.printf("SearchNumberTask: Number %d found in position %d\n", number, i);
                    taskManager.cancelTasks(this);
                    return i;
                }
                TimeUnit.SECONDS.sleep(1);
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
