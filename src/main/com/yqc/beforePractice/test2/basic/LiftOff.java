package com.yqc.beforePractice.test2.basic;
/**
 * 定义一个任务
 * @author Administrator
 *
 */
public class LiftOff implements Runnable {
	protected int countDown = 10;
	private static int taskCount = 0;
	private final int id = taskCount++;

	public LiftOff() {
	}

	public LiftOff(int countDown) {
		this.countDown = countDown;
	}

	@Override
	public void run() {
		while (countDown-- > 0) {
			System.out.println(status());
			Thread.yield();
		}
	}

	public String status() {
		return "#" + id + "(" + (countDown > 0 ? countDown : "LiftOff!") + ").";
	}
}
