package com.yqc.exception;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NativeExceptionHandling {
	public static void main(String[] args) {
		ExecutorService exec = Executors.newCachedThreadPool();
		try {
			exec.execute(new ExceptionThread());  //这里的异常时捕获不到的
		} catch (RuntimeException e) {
			System.out.println("Exception has been handled!");
		}
	}
}
