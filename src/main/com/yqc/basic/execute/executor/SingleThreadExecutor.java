package com.yqc.basic.execute.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * 只创建一个线程
 *
 * @author yangqc
 * 2016年7月20日
 */
public class SingleThreadExecutor {
	public static void main(String[] args) {
		ExecutorService exec=Executors.newSingleThreadExecutor();
		for(int i=0;i<5;i++){
			exec.execute(new LiftOff());
		}
		exec.shutdown();
	}
}
