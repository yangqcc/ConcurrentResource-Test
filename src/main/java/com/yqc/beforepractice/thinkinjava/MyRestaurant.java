package com.yqc.beforepractice.thinkinjava;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import util.concurrent.Executors;

class MyMeal {
    private int orderNum;

    MyMeal(int orderNum) {
        this.orderNum = orderNum;
    }

    public String toString() {
        return "OrderNum is " + orderNum;
    }
}

class MyChief implements Runnable {

    private MyRestaurant restaurant;
    private int count = 0;

    public MyChief(MyRestaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    while (restaurant.meals.size() >= restaurant.mealNum) {
                        wait();
                    }
                }
                synchronized (restaurant.myWaiter) {
                    MyMeal myMeal;
                    while (restaurant.meals.size() < restaurant.mealNum) {
                        restaurant.meals.addLast(myMeal = new MyMeal(++count));
                        System.out.println("Chief: " + myMeal);
                    }
                    restaurant.myWaiter.notifyAll();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("MyChief is Interrupted!");
        }
    }
}

class MyWaiter implements Runnable {

    private MyRestaurant restaurant;

    public MyWaiter(MyRestaurant restaurant) {
        this.restaurant = restaurant;
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                synchronized (this) {
                    while (restaurant.meals.size() == 0) {
                        wait();
                    }
                }
                synchronized (restaurant.myChief) {
                    if (restaurant.meals.size() > 0) {
                        System.out.println("Waiter: " + restaurant.meals.removeFirst());
                    }
                    while (restaurant.meals.size() == 0) {
                        System.out.println(restaurant.meals.size() + ": SIZE!");
                        restaurant.exec.shutdownNow();
                    }
                    restaurant.myChief.notifyAll();
                }
            }
        } catch (InterruptedException e) {
            System.out.println("MyWaiter is interrupted!");
        }
    }
}

public class MyRestaurant {
    final LinkedList<MyMeal> meals;
    MyChief myChief = new MyChief(this);
    MyWaiter myWaiter = new MyWaiter(this);
    ExecutorService exec = Executors.newCachedThreadPool();
    int mealNum;

    public MyRestaurant(int mealNum) {
        this.meals = new LinkedList<>();
        this.mealNum = mealNum;
        exec.execute(myChief);
        exec.execute(myWaiter);
    }

    public static void main(String[] args) {
        new MyRestaurant(100);
    }
}
