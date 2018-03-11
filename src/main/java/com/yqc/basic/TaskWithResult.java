package com.yqc.basic;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.Future;

public class TaskWithResult implements Callable<String> {
    private int id;

    public TaskWithResult(int id) {
        this.id = id;
    }

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        ArrayList<Future<String>> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            results.add(exec.submit(new TaskWithResult(i)));
        }
        for (Future<String> fs : results) {
            try {
                if (fs.isDone()) {  //判断任务是否完成，当然调用fs.get()是可以不判断，如果任务没有完成，get()方法将会阻塞
                    System.out.println(fs.get());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                exec.shutdown();
            }
        }
    }

    @Override
    public String call() throws Exception {
        return "result of TaskWithResult " + id;
    }
}
