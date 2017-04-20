package com.yqc.chapter5;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Semaphore(信号量)可以将一种容器变成右边界阻塞容器
 *
 * @author yangqc 2016年7月31日
 * @param <T>
 */
public class BoundHashSet<T> {
	private final Set<T> set;
	private final Semaphore sem;

	public BoundHashSet(int bound) {
		this.set = Collections.synchronizedSet(new HashSet<T>());
		this.sem = new Semaphore(bound);
	}

	public boolean add(T o) throws InterruptedException {
		sem.acquire();
		boolean wasAdded = false;
		try {
			wasAdded = set.add(o);
			return wasAdded;
		} finally {
			if (!wasAdded) {  //如果没有分配，则释放信号量
				sem.release();
			}
		}
	}

	public boolean remove(Object o) {
		boolean wasRemoved = set.remove(o);
		if (wasRemoved) {
			sem.release();
		}
		return wasRemoved;
	}
}
