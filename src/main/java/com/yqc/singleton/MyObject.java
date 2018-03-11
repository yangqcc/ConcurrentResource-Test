package com.yqc.singleton;

import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

/**
 * 单例模式  立即加载
 *
 * @author yangqc
 * 2016年8月24日
 */
public class MyObject {
    private static MyObject myObject = new MyObject();   //类加载时变创建

    private MyObject() {
        System.out.println("创建!");
    }

    public static MyObject getInstance() {
        return myObject;
    }

    public static void main(String[] args) {
        ExecutorService exec = Executors.newCachedThreadPool();
        Runnable myRunnable = new MyRunnable();
        for (int i = 0; i < 10; i++) {
            exec.execute(myRunnable);
        }
        exec.shutdown();
    }
}

class MyRunnable implements Runnable {

    @Override
    public void run() {
        try {
            Class.forName("com.yqc.singleton.MyObject");
            System.out.println(MyObject.getInstance().hashCode());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}