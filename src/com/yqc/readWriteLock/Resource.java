package com.yqc.readWriteLock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
/**
 * 测试读写锁
 *
 * @author yangqc
 * 2016年8月18日
 */
public class Resource {
	private int value;

	public void setValue(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static void main(String[] args) {
		ReadWriteLock lock = new ReentrantReadWriteLock();
		final Lock readLock = lock.readLock();
		final Lock writeLock = lock.writeLock();
		final Resource resource = new Resource();
		for (int i = 0; i < 20; i++) { // 写线程
			new Thread() {
				public void run() {
					writeLock.lock();
					try {
						resource.setValue(resource.getValue() + 1);
						System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - "
								+ Thread.currentThread() + "获取了写锁，修正数据为:" + resource.getValue());
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						writeLock.unlock();
					}
				}
			}.start();
		}
		for (int i = 0; i < 20; i++) {
			new Thread() {
				public void run() {
					readLock.lock();
					try {
						System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - "
								+ Thread.currentThread() + "获取了读锁，读取的数据为：" + resource.getValue());
						Thread.sleep(1000);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						readLock.unlock();
					}
				}
			}.start();
		}
	}
}
