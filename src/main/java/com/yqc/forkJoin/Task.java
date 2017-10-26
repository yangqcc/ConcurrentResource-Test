package com.yqc.forkJoin;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * ��ʹForkJoinPool�౻��Ƴ�����ִ��һ��ForkJoinTask����Ҳ����ֱ��ִ��Runnable��Callable����
 * ��Ҳ����ʹ��ForkJoinTask���adapt()������ִ������������һ��Callable�����Runnable������Ϊ������
 * ������һ��ForkJoinTask����
 *
 * RecursiveAction, RecursiveTask ����ForkJoinTask������
 *
 * @author yangqc 2016��8��21��
 */
public class Task extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private List<Production> productions;
	private int first;
	private int last;
	private double increment;

	public Task(List<Production> productions, int first, int last, double increment) { // increment������
		this.productions = productions;
		this.first = first;
		this.last = last;
		this.increment = increment;
	}

	@Override
	protected void compute() {
		if (last - first < 10) {
			updatePrices();
		} else {
			int middle = (last + first) / 2;
			System.out.printf("Task:Pending tasks:%s\n", getQueuedTaskCount());
			Task t1 = new Task(productions, first, middle + 1, increment);
			Task t2 = new Task(productions, middle + 1, last, increment);
			invokeAll(t1, t2);
			// invokeAll(ForkJoinTask<?>... tasks)������汾�ķ���ʹ��һ���ɱ�����б�����Դ����������Ҫִ�е�ForkJoinTask������Ϊ������
		}
	}

	private void updatePrices() {
		for (int i = first; i < last; i++) {
			Production product = productions.get(i);
			product.setPrice(product.getPrice() * (1 + increment));
		}
	}

	public static void main(String[] args) {
		ProductListGenerator generator = new ProductListGenerator();
		List<Production> productions = generator.generate(10000);
		Task task = new Task(productions, 0, productions.size(), 0.20);
		ForkJoinPool pool = new ForkJoinPool(); // ����ʹ�����޲ι�����
		pool.execute(task);
		do {
			System.out.printf("Main: Thread Count: %d\n", pool.getActiveThreadCount());
			System.out.printf("Main: Thread Steal: %d\n", pool.getStealCount());
			System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
			try {
				TimeUnit.MILLISECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while (!task.isDone());   //������ɺ����̲߳�ִ������Ĵ���
		pool.shutdown();
		if (task.isCompletedNormally()) {
			System.out.printf("Main: The process has completed normally.\n");
		}
		for (int i = 0; i < productions.size(); i++) {
			Production product = productions.get(i);
			if (product.getPrice() != 12) { // ���۸�û������20%�Ĳ�Ʒ��Ϣ��ӡ����
				System.out.printf("Product %s: %f\n", product.getName(), product.getPrice());
			}
		}
		System.out.println("Main: End of the program.\n");
	}
}
