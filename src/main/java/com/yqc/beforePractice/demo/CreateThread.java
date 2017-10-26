package com.yqc.beforePractice.demo;

public class CreateThread {
    public static void main(String[] args) {
        //第一种线程创建方式
        Thread thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("1:" + Thread.currentThread().getName());
                    System.out.println("2:" + this.getName());
                }
            }
        };

//		thread.start();


        //第二种线程创建方式
        Thread thread2 = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("1:" + Thread.currentThread().getName());
                }
            }
        });
//		thread2.start();


        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("runnable:" + Thread.currentThread().getName());
                }
            }
        }) {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread:" + Thread.currentThread().getName());
                }
            }
        }.start();
    }

}
