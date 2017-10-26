package java.util.practice.chapter8;

import java.util.concurrent.ThreadFactory;

/**
 * �Զ����̹߳��������߳��Լ�������
 *
 * @author yangqc 
 * 2016��8��7��
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
