package com.yqc.beforePractice.thinkinjava;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * �쳣���ݣ����񲻵�
 * 2015.12.30
 * @author Administrator
 *
 */
public class ExceptionThread implements Runnable{

	@Override
	public void run() {
		throw new RuntimeException();
	}
	public static void main(String[] args) {
		try{
		ExecutorService exec=Executors.newCachedThreadPool();
		exec.execute(new ExceptionThread());
		}catch(Exception e){
			System.out.println("Exception has been handled!");
		}
	}
}
