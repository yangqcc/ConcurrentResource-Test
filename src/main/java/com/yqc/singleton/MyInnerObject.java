package com.yqc.singleton;

/**
 * 通过内部类实现单例模式（其实也就是一个class只会被加载一次这和原理）
 *
 * @author yangqc
 * 2016年8月24日
 */
public class MyInnerObject {
    private MyInnerObject() {
    }

    public static MyInnerObject getInstance() {
        return MyObjectHandler.myInnerObject;
    }

    private static class MyObjectHandler {
        private static MyInnerObject myInnerObject = new MyInnerObject();
    }
}
