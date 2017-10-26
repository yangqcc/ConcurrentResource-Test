package com.yqc.beforePractice.demo;

public class TestDemo {
    public static void main(String[] args) {
        final p obj = new p();
        new Thread(() -> obj.p(obj.s())).start();

        new Thread(() -> obj.p(obj.s())).start();
    }

    static class p {
        public int s() {
            int a = 1;
            return 1;
        }

        public int p(int c) {
            for (long i = 0; i < 1000; i++) {
                c++;
                System.out.println(Thread.currentThread().getName() + " :" + c);
            }
            return c;
        }
    }
}
