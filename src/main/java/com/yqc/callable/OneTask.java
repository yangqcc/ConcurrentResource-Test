package com.yqc.callable;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @author yangqc
 * @date 2017/10/30
 */
public class OneTask implements Callable<String> {

    private int id;

    public OneTask(int id) {
        this.id = id;
    }

    @Override
    public String call() throws Exception {
        int i = 5;
        while (i >= 0) {
            System.out.println("Task " + id + " is working");
            Thread.sleep(1000);
            i--;
        }
        return "result of Test2 " + id;
    }
}

class TestMain {
    public static void main(String[] args) {
        Callable<String> callable = new OneTask(1);
        FutureTask<String> ft = new FutureTask<>(callable);
        new Thread(ft).start();
        //轮询检测线程是否执行完了
        while (!ft.isDone()) {
            try {
                System.out.println("检查线程执行完了吗...");
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        String result = "";
        try {
            //如果线程没有执行完成，get方法会一直阻塞在这里
            result = ft.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
        System.out.println("restart");
    }
}
