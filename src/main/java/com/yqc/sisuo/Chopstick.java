package com.yqc.sisuo;

/**
 * ���һ�����ӱ�ʹ�ã����ȡ�ÿ��ӵ��߳��������������drop�����������߳�
 *
 * @author yangqc 2016��7��26��
 */
public class Chopstick {
	private boolean taken = false;

	public synchronized void take() throws InterruptedException {
		while (taken) { // �����������Ѿ������������ǰ������߳�
			wait();
		}
		taken = true;
	}

	public synchronized void drop() {
		taken = false; // ��ǰ����ʹ����ϣ����������ȴ����߳�
		notifyAll();
	}
}
