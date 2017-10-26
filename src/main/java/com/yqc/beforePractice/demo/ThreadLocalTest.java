package com.yqc.beforePractice.demo;

import java.util.Random;

/**
 * ThreadLocal
 * 2015.10.26
 * @author Administrator
 *
 */
public class ThreadLocalTest {
	static ThreadLocal<Integer> x=new ThreadLocal<>();
	static ThreadLocal<MyThreadScopeData> myThreadScopeData=new ThreadLocal<>();
	public static void main(String[] args) {
		for(int i=0;i<2;i++){
			new Thread(new Runnable(){
				@Override
				public void run() {
					int data=new Random().nextInt();
					System.out.println(Thread.currentThread().getName()+" has put data :"+data);
					x.set(data);
//					MyThreadScopeData myData=new MyThreadScopeData();
//					myData.setName("name"+data);
//					myData.setAge(data);
//					myThreadScopeData.set(myData);

					MyThreadScopeData.getInstance().setAge(19);
					MyThreadScopeData.getInstance().setName("hello!");
					new A().get();
					new B().get();
				}
			}).start();
		}
	}

	static class A{
		public void get(){
//			int data=x.get();
//			MyThreadScopeData myData=myThreadScopeData.get();
			MyThreadScopeData myData=MyThreadScopeData.getInstance();
			System.out.println("A from "+Thread.currentThread().getName()+" get data: "+myData.getName()+myData.getAge());
		}
	}

	static class B{
		public void get(){
			int data=x.get();
			MyThreadScopeData myData=MyThreadScopeData.getInstance();
			System.out.println("B from "+Thread.currentThread().getName()+" get data :"+data);
		}
	}
}
//创建单例(两种模式)
class MyThreadScopeData{
	private MyThreadScopeData(){}
	public static /*synchronized*/ MyThreadScopeData getInstance(){
		MyThreadScopeData instance=map.get();
		if(instance==null){
			instance=new MyThreadScopeData();
			map.set(instance);
		}
		return instance;
	}
	//	private static MyThreadScopeData instance=new MyThreadScopeData();
	//private static MyThreadScopeData instance=null;
	private static ThreadLocal<MyThreadScopeData> map=new ThreadLocal<>();

	private String name;
	private int age;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}

}