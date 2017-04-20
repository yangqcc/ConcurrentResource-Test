package com.yqc.execute;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * TimerTask抛出未检查的异常时将终止定时线程，Timer不会恢复线程的执行，而是错误的 认为整个Timer都被取消掉了。
 * 所以该程序执行一秒就结束了
 *
 * @author yangqc 2016年8月1日
 */
public class OutOfTime {
	public static void main(String[] args) throws InterruptedException {
		Timer timer = new Timer();
		timer.schedule(new ThrowTask(), 1);
		TimeUnit.SECONDS.sleep(1);
		timer.schedule(new ThrowTask(), 1);
		TimeUnit.SECONDS.sleep(5);
	}

	static class ThrowTask extends TimerTask {
		@Override
		public void run() {
			throw new RuntimeException();
		}
	}
}
