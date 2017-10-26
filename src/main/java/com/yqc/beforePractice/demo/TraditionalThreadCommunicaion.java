package com.yqc.beforePractice.demo;

public class TraditionalThreadCommunicaion {
    public static void main(String[] args) {
//		new Thread(new Runnable(){
//
//			@Override
//			public void run() {
//				for(int i=0;i<50;i++){
//					synchronized (TraditionalThreadCommunicaion.class) {
//						for(int j=0;j<10;j++){
//							System.out.println("sub thread sequence of +"+j+",loop of "+i);
//						}
//					}
//				}
//			}
//			
//		}).start();
//		
//		for (int i = 0; i < 50; i++) {
//			synchronized (TraditionalThreadCommunicaion.class) {
//				for (int j = 0; j < 10; j++) {
//					System.out.println("sub thread sequence of " + j + ",loop of "+ i);
//				}
//			}
//		}

        final Business business = new Business();
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 50; i++) {
                    business.sub(i);
                }
            }

        }
        ).start();
        for (int i = 0; i < 50; i++) {
            business.main(i);
        }
    }

    static class Business {
        private boolean bShouldSub = true;

        public synchronized void sub(int num) {
//			if (bShouldSub) {
            while (bShouldSub) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < 10; i++) {
                System.out.println("sub thread sequence if " + i + " "
                        + num);
            }
            bShouldSub = false;
            this.notify();
        }

        public synchronized void main(int num) {
//			if (!bShouldSub) {
            while (!bShouldSub) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < 100; i++) {
                System.out.println("main thread sequence if " + i + " "
                        + num);
            }
            bShouldSub = true;
            this.notify();
        }
    }
}
