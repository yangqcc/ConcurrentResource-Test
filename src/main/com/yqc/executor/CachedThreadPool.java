package com.yqc.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadPool {
	public static void main(String[] args) {
		ExecutorService exec=Executors.newCachedThreadPool();  //单个ExecutorService被用来创建和管理系统中的所有任务
		for(int i=0;i<5;i++){
			exec.execute(new LiftOff());
		}
		exec.shutdown();
	}
}
