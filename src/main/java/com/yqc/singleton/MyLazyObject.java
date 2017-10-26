package com.yqc.singleton;
/**
 * ����   ����ģʽ
 *
 * @author yangqc
 * 2016��8��24��
 */
public class MyLazyObject {
	private static MyLazyObject myObject;
	
	private MyLazyObject(){}
	
	public static MyLazyObject getInstance() {
		if (myObject != null) {   //��ֹ�����̰߳�ȫ����
		} else {
			synchronized (MyLazyObject.class) {
				if (myObject == null) {
					myObject = new MyLazyObject();
				}
			}
		}
		return myObject;
	}
}
