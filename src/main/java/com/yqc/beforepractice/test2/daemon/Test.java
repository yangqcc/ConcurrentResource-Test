package com.yqc.beforepractice.test2.daemon;

public class Test {
    public static void main(String[] args) {
        int a = 2;
        int b = 3;
        switch (a) {
            default:
                b++;
            case 10:
                b++;
            case 3:
                b++;
                b++;
                break;
        }

    }
}
