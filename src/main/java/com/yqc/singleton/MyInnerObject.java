package com.yqc.singleton;
/**
 * ͨ���ڲ���ʵ�ֵ���ģʽ����ʵҲ����һ��classֻ�ᱻ����һ�����ԭ��
 *
 * @author yangqc
 * 2016��8��24��
 */
public class MyInnerObject {
	private static class MyObjectHandler {
		private static MyInnerObject myInnerObject = new MyInnerObject();
	}

	private MyInnerObject() {
	}

	public static MyInnerObject getInstance() {
		return MyObjectHandler.myInnerObject;
	}
}
