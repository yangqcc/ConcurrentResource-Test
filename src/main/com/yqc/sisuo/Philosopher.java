package com.yqc.sisuo;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Philosopher implements Runnable {

	private Chopstick left;
	private Chopstick right;
	private final int id;
	private final int ponderFactor;
	private Random rand = new Random(47);

	// 如果ponderFactor不为0，则pause()方法会休眠一段随机时间
	// 然后获取(take())右边和左边的Chopstick，然后吃饭花掉一段随机时间，之后重复此过程
	private void pause() throws InterruptedException {
		if (ponderFactor == 0) {
			return;
		}
		TimeUnit.MICROSECONDS.sleep(rand.nextInt(ponderFactor * 250));
	}

	public Philosopher(Chopstick left, Chopstick right, int ident, int ponder) {
		this.left = left;
		this.right = right;
		id = ident;
		ponderFactor = ponder;
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				System.out.println(this + " " + "thinking");
				pause();
				right.take();
				System.out.println(this + " grabbing right");
				left.take();
				System.out.println(this + " grabing left");
				
				
				// 下面这种实现就是一次性给一个哲学家分配两只筷子
				// synchronized (left) {
				// synchronized (right) {
				// right.take();
				// System.out.println(this + " grabbing right");
				// left.take();
				// System.out.println(this + " grabing left");
				// }
				// }

				System.out.println(this + " eating");
				pause();
				right.drop();
				left.drop();
			}
		} catch (InterruptedException e) {
			System.out.println(this + " exiting via interrupt");
		}
	}

	public String toString() {
		return "Philosopher " + id;
	}
}
