//package com.yqc.Resource;
//
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//class CircularSet {
//	private int[] array;
//	private int len;
//	private int index = 0;
//
//	public CircularSet(int size) {
//		array = new int[size];
//		len = size;
//		for (int i = 0; i < size; i++) {
//			array[i] = -1;
//		}
//	}
//
//	public synchronized void add(int i) {
//		array[index] = i;
//		index = ++index % len;
//	}
//}
//
//public class SerivalNumberChecker {
//	private static final int SIZE=10;
//	private static CircularSet serials=new CircularSet(1000);
//	private static ExecutorService exec=Executors.newCachedThreadPool();
//	static class SerialChecker implements Runnable{
//
//		@Override
//		public void run() {
//			while(true){
//				int serial=SerivalNumberGenerator().
//			}
//		}
//		
//	}
//
//}
