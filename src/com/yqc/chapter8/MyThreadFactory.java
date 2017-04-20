package com.yqc.chapter8;

import java.util.concurrent.ThreadFactory;

/**
 * 自定义线程工厂，有线程自己的名字
 *
 * @author yangqc 
 * 2016年8月7日
 */
public class MyThreadFactory implements ThreadFactory {
	private final String poolName;

	public MyThreadFactory(String poolName) {
		this.poolName = poolName;
	}

	@Override
	public Thread newThread(Runnable arg0) {
		return null;
	}

}
