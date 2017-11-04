package com.yqc.forkjoin.saleproduct;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * fork-join异步执行
 * Created by yangqc on 2017/10/28
 */
public class FolderProcessor extends RecursiveTask<List<String>> {

    private static final long serialVersionUID = 1L;
    private String path;
    private String extension;

    public FolderProcessor(String path, String extension) {
        this.path = path;
        this.extension = extension;
    }

    @Override
    protected List<String> compute() {
        List<String> list = new ArrayList<>();
        List<FolderProcessor> tasks = new ArrayList<>();
        File file = new File(path);
        File[] contents = file.listFiles();
        if (contents != null) {
            Arrays.stream(contents).forEach(s -> {
                if (s.isDirectory()) {
                    FolderProcessor task = new FolderProcessor(s.getAbsolutePath(), extension);
                    task.fork();
                    tasks.add(task);
                } else {
                    if (checkFile(s.getName())) {
                        list.add(s.getAbsolutePath());
                    }
                }
            });
        }
        addResultForTasks(list, tasks);
        return list;
    }

    private boolean checkFile(String name) {
        return name.endsWith(extension);
    }

    private void addResultForTasks(List<String> list, List<FolderProcessor> tasks) {
        tasks.stream().forEach(task -> list.addAll(task.join()));
    }
}

class TestMain {
    public static void main(String[] args) {
        //创建线程池
        ForkJoinPool pool = new ForkJoinPool();
        //创建三个任务
        FolderProcessor system = new FolderProcessor("C:\\Windows", "log");
        FolderProcessor apps = new FolderProcessor("C:\\Program Files", "log");
        FolderProcessor documents = new FolderProcessor("C:\\Documents And Settings", "log");
        //执行任务
        pool.execute(system);
        pool.execute(apps);
        pool.execute(documents);
        //显示线程进展信息
        try {
            while ((!system.isDone()) || (!apps.isDone()) || (!documents.isDone())) {
                System.out.println("***************************************************");
                System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
                System.out.printf("Main: Active Threads: %d\n", pool.getActiveThreadCount());
                System.out.printf("Main: Task Count: %d\n", pool.getQueuedTaskCount());
                System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
                System.out.println("***************************************************");
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //关闭线程池
        pool.shutdown();
        List<String> results;
        results = system.join();
        System.out.printf("System: %d files found.\n", results.size());
        results = apps.join();
        System.out.printf("Apps: %d files found.\n", results.size());
        results = documents.join();
        System.out.printf("Documents: %d files found.\n", results.size());
    }
}
