package java.util.practice.chapter10;

import java.util.Random;

public class DemonstrateDeadlock {
    private static final int NUM_THREADS = 20;
    private static final int NUM_ACCOUNT = 5;
    private static final int NUM_ITERATIONS = 1000000;

    public static void main(String[] args) {
        final Random rnd = new Random();
        final Object[] accounts = new Object[NUM_ACCOUNT];
        for (int i = 0; i < accounts.length; i++) {
            accounts[i] = new Object();
        }

        class TransferThread extends Thread {
            public void run() {
                for (int i = 0; i < NUM_ITERATIONS; i++) {
                    int fromAcct = rnd.nextInt(NUM_ACCOUNT);
                    int toAcc = rnd.nextInt(NUM_ACCOUNT);

                }
            }
        }

        for (int i = 0; i < NUM_THREADS; i++) {
            new TransferThread().start();
        }
    }
}
