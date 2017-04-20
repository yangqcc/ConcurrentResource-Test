package com.yqc.forkJoin;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * 即使ForkJoinPool类被设计成用来执行一个ForkJoinTask，你也可以直接执行Runnable和Callable对象。
 * 你也可以使用ForkJoinTask类的adapt()方法来执行任务，它接收一个Callable对象或Runnable对象（作为参数）
 * 并返回一个ForkJoinTask对象
 *
 * RecursiveAction, RecursiveTask 都是ForkJoinTask的子类
 *
 * @author yangqc 2016年8月21日
 */
public class Task extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	private List<Production> productions;
	private int first;
	private int last;
	private double increment;

	public Task(List<Production> productions, int first, int last, double increment) { // increment增长率
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
			// invokeAll(ForkJoinTask<?>... tasks)：这个版本的方法使用一个可变参数列表。你可以传入许多你想要执行的ForkJoinTask对象作为参数。
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
		ForkJoinPool pool = new ForkJoinPool(); // 这里使用了无参构造器
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
		} while (!task.isDone());   //任务完成后，主线程才执行下面的代码
		pool.shutdown();
		if (task.isCompletedNormally()) {
			System.out.printf("Main: The process has completed normally.\n");
		}
		for (int i = 0; i < productions.size(); i++) {
			Production product = productions.get(i);
			if (product.getPrice() != 12) { // 将价格没有增长20%的产品信息打印出来
				System.out.printf("Product %s: %f\n", product.getName(), product.getPrice());
			}
		}
		System.out.println("Main: End of the program.\n");
	}
}
