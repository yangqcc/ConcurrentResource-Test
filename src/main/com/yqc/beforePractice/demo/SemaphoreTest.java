package com.yqc.beforePractice.demo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class SemaphoreTest {
	public static void main(String[] args) {
		ExecutorService service=Executors.newCachedThreadPool();
		final Semaphore sp=new Semaphore(3);
		for(int i=0;i<10;i++){
			Runnable runnable=new Runnable(){
				@Override
				public void run() {
					try{
						sp.acquire();
					}catch(InterruptedException e1){
						e1.printStackTrace();
					}
					System.out.println();
				}};
		}
	}
}
