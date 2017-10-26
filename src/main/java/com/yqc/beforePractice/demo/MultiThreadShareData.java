package com.yqc.beforePractice.demo;

public class MultiThreadShareData {
	public static void main(String[] args) {
		ShareData1 data1=new ShareData1();
		new Thread(data1).start();
		new Thread(data1).start();
	}
}
class ShareData1 implements Runnable{
	private int count=100;
	int j=0;
	public synchronized void increment(){
		j++;
	}
	public synchronized void decrement(){
		j--;
	}
	@Override
	public void run() {
		while(count>=0){
			System.out.println(Thread.currentThread().getName()+":"+count);
			count--;
		}
	}
}