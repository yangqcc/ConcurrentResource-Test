package com.yqc;

/**
 * 线程不可见性
 */
public class NoVisibility {

  private static boolean ready = false;
  private static int number = 12;

  public static void main(String[] args) {
    new ReaderThread().start();
    number = 42;
    ready = true;
  }

  private static class ReaderThread extends Thread {

    public void run() {
      while (!ready) {
        Thread.yield();
      }
      System.out.println(number);
    }
  }
}
