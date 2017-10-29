package com.yqc.forkJoin.cancel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * Created by yangqc on 2017/10/29
 */
public class TaskManager {

    private List<ForkJoinTask<Integer>> tasks;

    public TaskManager() {
        tasks = new ArrayList<>();
    }

    public void addTask(ForkJoinTask<Integer> task) {
        tasks.add(task);
    }

    public void cancelTasks(ForkJoinTask<Integer> cancelTask) {
        tasks.forEach(task->{
            if(task!=cancelTask){
                task.cancel(true);
                ((SearchN)task).w
            }
        });
    }
}