package com.yqc.singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 错误的单例模式
 *
 * @author yangqc 2016年8月24日
 */
public class MyErrorObject {
	private static MyErrorObject myErrorObject;

	private MyErrorObject() {
	}

	public static MyErrorObject getInstance() {
		if (myErrorObject != null) {
		} else {
			try {
				Thread.sleep(1000);
				synchronized (MyErrorObject.class) {  //这里就避免了线程同步问题
					if (myErrorObject == null) {
						myErrorObject = new MyErrorObject();
					}
				}
			} catch (InterruptedException e) {
//				myErrorObject = new MyErrorObject();
				System.out.println("出错了!");
			}
		}
		return myErrorObject;
	}

	public static void main(String[] args) {
		ExecutorService exec=Executors.newCachedThreadPool();
		Runnable myRunnable=new MyRunnable2();
		for(int i=0;i<100;i++){
			exec.execute(myRunnable);
		}
		exec.shutdown();
	}
}

class MyRunnable2 implements Runnable {

	@Override
	public void run() {
		System.out.println(MyErrorObject.getInstance().hashCode());
	}
}