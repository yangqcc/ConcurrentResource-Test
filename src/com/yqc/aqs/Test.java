package com.yqc.aqs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {
	public static void main(String[] args) throws InterruptedException {
		ExecutorService exec = Executors.newFixedThreadPool(10);
		exec.execute(new Runnable() {
			@Override
			public void run() {
//				while (true) {
//					if (!Thread.currentThread().isInterrupted()) {
//						return ;
//					}
//					System.out.println(Thread.currentThread().isInterrupted());
//				}
				try {
					Thread.sleep(5000);
					System.out.println("tuichu1");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					System.out.println("99");
					e.printStackTrace();
				}
			}
		});
		Thread.sleep(3000);
		exec.shutdown();
	}
}
