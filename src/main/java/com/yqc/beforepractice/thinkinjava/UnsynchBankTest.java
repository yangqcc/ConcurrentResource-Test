package com.yqc.beforepractice.thinkinjava;

public class UnsynchBankTest {
    public static final int NACCOUNTS = 100;
    public static final double INITAL_BALANCE = 1000;

    public static void main(String args[]) {
        Bank b = new Bank(NACCOUNTS, INITAL_BALANCE);
        int i;
        for (i = 0; i < NACCOUNTS; i++) {
            TransferRunnable r = new TransferRunnable(b, i, INITAL_BALANCE);
            Thread t = new Thread(r);
            t.start();
        }
    }
}
