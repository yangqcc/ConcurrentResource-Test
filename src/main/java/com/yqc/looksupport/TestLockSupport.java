package looksupport;

import util.concurrent.locks.LockSupport;

/**
 * LockSupport会响应中断，也就是说，调用park，该线程阻塞，如果此期间 线程被中断，那么会从阻塞中恢复，但是只会设置中断标志位，不会抛出异常
 * 这时候需要我们设置中断响应策略
 *
 * @author yangqc 2016年9月21日
 */
public class TestLockSupport {

  public static void main(String[] args) throws InterruptedException {
    Thread thread = new Thread(new LockSupportTask());
    thread.start();
    Thread.sleep(3000);
    //中断线程,会自动从阻塞中恢复,并且设置标志位
    thread.interrupt();
//    LockSupport.unpark(thread);
  }
}

class LockSupportTask implements Runnable {

  @Override
  public void run() {
    int i = 0;
    while (true) {
      if (Thread.currentThread().isInterrupted()) {
        break;
      }
      if (i == 0) {
        LockSupport.park();
      }
      i++;
    }
    System.out.println("娘的，老子被中断了,循环次数:" + i);
  }
}