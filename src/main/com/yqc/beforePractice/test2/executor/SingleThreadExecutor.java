package com.yqc.beforePractice.test2.executor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.yqc.beforePractice.test2.LiftOff;
import com.yqc.basic.execute.executor.LiftOff;

public class SingleThreadExecutor {
	public static void main(String[] args) {
		ExecutorService exec = Executors.newSingleThreadExecutor();
		for (int i = 0; i < 5; i++)
			exec.execute(new LiftOff());
		exec.shutdown();
	}
}
