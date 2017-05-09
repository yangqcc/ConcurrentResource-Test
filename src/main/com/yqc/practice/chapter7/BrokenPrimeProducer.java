package com.yqc.practice.chapter7;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
/**
 * 如果队列阻塞，那么线程将永远不会停止
 *
 * @author yangqc
 * 2016年8月2日
 */
public class BrokenPrimeProducer extends Thread {
	private final BlockingQueue<BigInteger> queue;
	private volatile boolean cancelled = false;

	BrokenPrimeProducer(BlockingQueue<BigInteger> queue) {
		this.queue = queue;
	}

	public void run() {
		try {
			BigInteger p = BigInteger.ONE;
			while (!cancelled) {
				queue.put(p = p.nextProbablePrime());
			}
		} catch (InterruptedException consumed) {

		}
	}

	public void cancel() {
		cancelled = true;
	}
	
	void consumePrimes(){
//		BlockingQueue<BigInteger> primes=new BlockingQueue<>();
	}
}
