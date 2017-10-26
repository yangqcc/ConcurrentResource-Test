package com.yqc.singleton;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * ����ģʽ  ��������
 *
 * @author yangqc
 * 2016��8��24��
 */
public class MyObject {
	private static MyObject myObject = new MyObject();   //�����ʱ�䴴��

	private MyObject() {
		System.out.println("����!");
	}

	public static MyObject getInstance() {
		return myObject;
	}

	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		Runnable myRunnable = new MyRunnable();
		for (int i = 0; i < 10; i++) {
			exec.execute(myRunnable);
		}
		exec.shutdown();
	}
}

class MyRunnable implements Runnable {

	@Override
	public void run() {
		try {
			Class.forName("com.yqc.singleton.MyObject");
			System.out.println(MyObject.getInstance().hashCode());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}