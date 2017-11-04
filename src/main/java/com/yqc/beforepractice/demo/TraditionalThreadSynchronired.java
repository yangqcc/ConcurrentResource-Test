package com.yqc.beforepractice.demo;

/**
 * 线程同步与安全
 * <p>
 * 2015.10.25
 *
 * @author Yangqc
 */
public class TraditionalThreadSynchronired {
    public static void main(String[] args) {
        new TraditionalThreadSynchronired().init();
    }

    private void init() {
        final Outputer outputer = new Outputer();
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output("qicheng!");
                }
            }

        }).start();

        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    outputer.output2("shijia!");
                }
            }

        }).start();
    }

    //内部类
    static class Outputer {
        String xxx = "";

        public static synchronized void output3(String name) {
            int len = name.length();
            for (int i = 0; i < len; i++) {
                System.out.print(name.charAt(i));
            }
            System.out.println("---------");
        }

        public void output(String name) {
            int len = name.length();
            synchronized (Outputer.class) {
                for (int i = 0; i < len; i++) {
                    System.out.print(name.charAt(i));
                }
                System.out.println("---------");
            }
        }

        public synchronized void output2(String name) {
            int len = name.length();
            for (int i = 0; i < len; i++) {
                System.out.print(name.charAt(i));
            }
            System.out.println("---------");
        }
    }
}
