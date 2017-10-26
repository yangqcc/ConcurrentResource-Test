package com.yqc.beforePractice.demo;
/**
 * 多个线程共享数据，就把共享数据放在一个对象里面，然后逐一传递给各个Runnableduixiang1
 *
 * 2015.10.26
 * @author Yangqc
 *
 */
public class MultiThreadShareData2 {
	private static ShareData2 data1=new ShareData2();
	public static void main(String[] args) {
//		final ShareData2 data1=new ShareData2();
//		new Thread(new MyRunnable1(data1)).start();
//		new Thread(new MyRunnable2(data1)).start();
		new Thread(new Runnable(){
			@Override
			public void run() {
				data1.decrement();
			}
		}).start();
		new Thread(new Runnable(){
			@Override
			public void run() {
				data1.increment();
			}
		}).start();
	}
}
class MyRunnable1 implements Runnable{
	private ShareData2 data1;
	public MyRunnable1(ShareData2 data1){
		this.data1=data1;
	}
	@Override
	public void run() {
		data1.decrement();
	}

}
class MyRunnable2 implements Runnable{
	private ShareData2 data1;
	public MyRunnable2(ShareData2 data1){
		this.data1=data1;
	}
	@Override
	public void run() {
		data1.increment();
	}

}

class ShareData2{
	int j=100;
	public void decrement(){
		j--;
	}
	public void increment(){
		j++;
	}
}