package com.yqc.singleton;
/**
 * 单例   懒汉模式
 *
 * @author yangqc
 * 2016年8月24日
 */
public class MyLazyObject {
	private static MyLazyObject myObject;

	private MyLazyObject(){}

	public static MyLazyObject getInstance() {
		if (myObject != null) {   //防止出现线程安全问题
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
