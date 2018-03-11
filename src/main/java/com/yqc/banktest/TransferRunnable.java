package com.yqc.banktest;

import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

public class TransferRunnable implements Runnable {
    private Bank bank;
    private double maxAccount;
    private int DELAY = 10;

    /**
     * 转账任务
     *
     * @param bank 银行
     * @param max  每次转账的金额
     */
    public TransferRunnable(Bank bank, double max) {
        this.bank = bank;
        maxAccount = max;
    }

    public static void main(String[] args) {
        Bank bank = new Bank(1000, 1000);
        TransferRunnable runnable = new TransferRunnable(bank, 1);
        ExecutorService exec = Executors.newCachedThreadPool();
        for (int i = 0; i < 100; i++) {
            exec.execute(runnable);
        }
        exec.shutdownNow();   //一定要加shutdownNow，否则可能出现所有线程一直等待的情况
    }

    @Override
    public void run() {
        try {
            while (true) {
                int toAccount = (int) (bank.size() * Math.random());
                int fromAccount = (int) (bank.size() * Math.random());
                double amount = maxAccount;
                bank.transfer(fromAccount, toAccount, amount);
                Thread.sleep((long) (DELAY * Math.random()));
            }
        } catch (InterruptedException e) {
            System.out.println(Thread.currentThread().getName() + " 线程结束!");
            return;
        }
    }
}
