//package com.yqc.newS;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.Delayed;
//import java.util.concurrent.TimeUnit;
//
//class DelayedTask implements Runnable, Delayed {
//
//	private static int counter = 0;
//	private final int id = counter++;
//	private final int delta;
//	private final long trigger;
//
//	protected static List<DelayedTask> sequence = new ArrayList<>();
//
//	public DelayedTask(int delayInMilliseconds) {
//		delta = delayInMilliseconds;
//		trigger = System.nanoTime();
//		sequence.add(this);
//	}
//
//	@Override
//	public int compareTo(Delayed arg0) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public long getDelay(TimeUnit arg0) {
//		return unit.;
//	}
//
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//
//	}
//}
//
//public class DeplayQueueDemo {
//
//}
