//package com.yqc.endThread;
//
//class NeedsCleanup {
//	private final int id;
//
//	public NeedsCleanup(int ident) {
//		id = ident;
//		System.out.println("NeedsCleanup " + id);
//	}
//}
//
//class Blocked3 implements Runnable{
//	private volatile double d=0.0;
//	
//	@Override
//	public void run() {
//		try{
//		while(!Thread.interrupted()){
//			NeedsCleanup n1=new NeedsCleanup(1);
//			
//		}
//		}
//	}}
//
//public class InterruptingIdiom {
//
//}
