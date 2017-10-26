package java.practice.chapter7;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
/**
 * ���������������ô�߳̽���Զ����ֹͣ
 *
 * @author yangqc
 * 2016��8��2��
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
