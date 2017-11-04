package com.yqc.beforepractice.demo;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时器
 * <p>
 * 2015.10.25
 *
 * @author Yangqc
 */
public class TraditionalTimerTest {
    private static int x = 0;

    public static void main(String[] args) {
//		new Timer().schedule(new TimerTask(){
//			@Override
//			public void run() {
//				System.out.println("bombing!");
//			}}, 1000,3000);
        class MyTimerTask extends TimerTask {

            @Override
            public void run() {
                x = (x + 1) % 2;
                System.out.println("bombing!");
                new Timer().schedule(new MyTimerTask(), 2000 + 2000 * x);
            }

        }

        new Timer().schedule(new MyTimerTask()
                /*new Timer().schedule(new TimerTask(){

					@Override
					public void run() {
						System.out.println("bombing!");
					}

				}, 2000);*/
//				new Timer().schedule(this, 2000);    //这一句有错误
                , 2000);
        while (true) {
            System.out.println(new Date().getSeconds());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
