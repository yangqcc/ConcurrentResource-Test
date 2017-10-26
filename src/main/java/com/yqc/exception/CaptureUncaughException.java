package com.yqc.exception;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * �����߳��쳣
 *
 * @author yangqc 2016��7��21��
 */
class ExceptionThread2 implements Runnable {  //����һ������

	@Override
	public void run() {
		Thread t = Thread.currentThread();
		System.out.println("*run() by " + t);
		System.out.println(" eh = " + t.getUncaughtExceptionHandler());
		throw new RuntimeException();
	}

}

class MyUncugtExceptionHandler implements Thread.UncaughtExceptionHandler {  //�����Լ����쳣

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.out.println("cautht" + e);
	}
}

class HandlerThreadFactory implements ThreadFactory { //�̹߳���

	@Override
	public Thread newThread(Runnable r) {
		System.out.println(this +  " creating new Thread");
		Thread t = new Thread(r);
		System.out.println("created " + t);
		t.setUncaughtExceptionHandler(new MyUncugtExceptionHandler());
		System.out.println("eh=" + t.getUncaughtExceptionHandler());
		return t;
	}
}

public class CaptureUncaughException {
	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool(new HandlerThreadFactory());
		exec.execute(new ExceptionThread2());
//		exec.shutdown();
	}
}
