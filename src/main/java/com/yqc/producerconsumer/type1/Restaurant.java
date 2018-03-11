package com.yqc.producerconsumer.type1;

import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-09
 * @modified By yangqc
 */
class Meal {

  /**
   * 订单序号
   */
  private final int orderNum;

  public Meal(int orderNum) {
    this.orderNum = orderNum;
  }

  @Override
  public String toString() {
    return "Meal " + orderNum;
  }
}

/**
 * 消费者
 */
class WaitPerson implements Runnable {

  private Restaurant restaurant;

  public WaitPerson(Restaurant r) {
    restaurant = r;
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        synchronized (this) {
          while (restaurant.meal == null) {
            wait(); // 等待chef生产meal
          }
        }
        System.out.println("Waitperson got " + restaurant.meal);
        synchronized (restaurant.chef) {
          restaurant.meal = null;
          restaurant.chef.notifyAll();// 通知chef继续生产
        }
      }
    } catch (InterruptedException e) {
      System.out.println("WaitPerson interrupted");
    }
  }
}

class Chef implements Runnable {

  private Restaurant restaurant;
  private int count = 0;

  public Chef(Restaurant r) {
    restaurant = r;
  }

  @Override
  public void run() {
    try {
      while (!Thread.interrupted()) {
        synchronized (this) {
          while (restaurant.meal != null) {
            wait();// 等待meal被拿走
          }
        }
        if (++count == 10) {
          System.out.println("Out of food,closing");
          restaurant.exec.shutdownNow();//向每个线程发送Interrupt
          return;//如果没有直接return 将多执行了下面的 Order up ,所以一般情况下都要直接return
        }

        System.out.println("Order up! ");
        synchronized (restaurant.waitPerson) {
          // 对notifyAll()的调用必须先获得waitPerson的锁
          restaurant.meal = new Meal(count);
          restaurant.waitPerson.notifyAll();
        }
        TimeUnit.MILLISECONDS.sleep(500);//休眠一下是为了给shutdownNow留出时间
      }
    } catch (InterruptedException e) {
      System.out.println("Chef interrupted");
    }
  }
}

public class Restaurant {

  Meal meal;
  ExecutorService exec = Executors.newCachedThreadPool();
  WaitPerson waitPerson = new WaitPerson(this);
  Chef chef = new Chef(this);

  public Restaurant() {
    exec.execute(chef);
    exec.execute(waitPerson);
  }

  public static void main(String[] args) {
    new Restaurant();
  }
}