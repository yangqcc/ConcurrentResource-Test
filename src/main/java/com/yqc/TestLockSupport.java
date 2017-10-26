package com.yqc;

import java.util.concurrent.locks.LockSupport;

/**
 * LockSupport����Ӧ�жϣ�Ҳ����˵������park�����߳�������������ڼ� �̱߳��жϣ���ô��������лָ�������ֻ�������жϱ�־λ�������׳��쳣
 * ��ʱ����Ҫ���������ж���Ӧ����
 *
 * @author yangqc 2016��9��21��
 */
public class TestLockSupport {
	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread(new LockSupportTask());
		thread.start();
		Thread.sleep(1000);
		thread.interrupt();
		// LockSupport.unpark(thread);
	}
}

class LockSupportTask implements Runnable {

	@Override
	public void run() {
		int i = 0;
		while (true) {
			try {
				if (Thread.currentThread().isInterrupted()) {
//					break;
				}
				if(i==0){
					Thread.sleep(100);
				}
				System.out.println("������Ҫ�������ˣ�");
				if (i == 0) {
					LockSupport.park();
				}
				i++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("xixi");
		}
//		System.out.println("��ģ����ӱ��ж���!");
	}
}