package com.yqc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/**
 * 抛光在打蜡任务完成之后，打蜡在另一次抛光完成之后
 *
 * @author yangqc
 * 2016年7月25日
 */
class Car {
	private boolean waxOn = false;

	public synchronized void waxed() {  
		waxOn = true;   //准备抛光
		notifyAll();
	}

	public synchronized void buffed() {   
		waxOn = false;   //准备另一次的抛光
		notifyAll();
	}

	public synchronized void waitForWaxing() throws InterruptedException {  //wax 打蜡 等待打蜡
		while (waxOn == false) {
			wait();
		}
	}

	public synchronized void waitForBuffing() throws InterruptedException {  //buff 等待抛光
		while (waxOn == true) {
			wait();
		}
	}
}

class WaxOn implements Runnable {
	private Car car;

	public WaxOn(Car c) {
		car = c;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			System.out.println("Wax On!");
			try {
				TimeUnit.SECONDS.sleep(2);
				car.waxed();   //打蜡结束
				car.waitForBuffing();  //等待抛光
			} catch (InterruptedException e) {
				System.out.println("Exiting via interrupt");
			}
			System.out.println("Ending Wax On task");
		}
	}
}

class WaxOff implements Runnable {

	private Car car;

	public WaxOff(Car c) {
		car = c;
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			try {
				car.waitForWaxing(); //等待打蜡，如果打蜡完成，则继续，进行抛光
				System.out.println("Wax Off!");
				TimeUnit.SECONDS.sleep(2);
				car.buffed();  //抛光结束
			} catch (InterruptedException e) {
				System.out.println("Exiting via interrupt");
			}
			System.out.println("Ending Wax Off task");
		}
	}
}

public class WaxOMatic {
	public static void main(String[] args) throws InterruptedException {
		Car car = new Car();
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(new WaxOff(car));
		exec.execute(new WaxOn(car));
		TimeUnit.SECONDS.sleep(5);
		exec.shutdownNow();
	}
}
