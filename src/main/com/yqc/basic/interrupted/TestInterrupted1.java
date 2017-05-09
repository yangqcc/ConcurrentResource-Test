package com.yqc.basic.interrupted;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TestInterrupted1 {
	private static final ScheduledExecutorService cancelExec = Executors.newScheduledThreadPool(1);

	public static void timedRun(final Runnable r, long timeout, TimeUnit unit) throws InterruptedException {
		class RethrowableTask implements Runnable {

			private volatile Throwable t;

			@Override
			public void run() {
				try {
					r.run();
				} catch (Throwable t) {
					this.t = t;
				}
			}

			void rethrow() {
				if (t != null) {
					throw new RuntimeException(t);
				}
			}
		}

		RethrowableTask task = new RethrowableTask();
		final Thread taskThread = new Thread(task);
		taskThread.start();
		cancelExec.schedule(new Runnable() {
			@Override
			public void run() {
				taskThread.interrupt();
			}
		}, timeout, unit);
		taskThread.join(unit.toMillis(timeout));
		task.rethrow();
	}
}
