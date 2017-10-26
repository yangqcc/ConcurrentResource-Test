package com.yqc.singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ����ĵ���ģʽ
 *
 * @author yangqc 2016��8��24��
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
				synchronized (MyErrorObject.class) {  //����ͱ������߳�ͬ������
					if (myErrorObject == null) {
						myErrorObject = new MyErrorObject();
					}
				}
			} catch (InterruptedException e) {
//				myErrorObject = new MyErrorObject();
				System.out.println("������!");
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