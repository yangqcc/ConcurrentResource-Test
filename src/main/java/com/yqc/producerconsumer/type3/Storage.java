package com.yqc.producerconsumer.type3;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */

import java.util.LinkedList;
import util.concurrent.locks.Condition;
import util.concurrent.locks.Lock;
import util.concurrent.locks.ReentrantLock;
import lombok.Getter;

public class Storage {

  // 仓库最大存储量
  @Getter
  private final int MAX_SIZE = 100;

  // 仓库存储的载体
  @Getter
  private LinkedList<Object> list = new LinkedList<>();

  // 锁
  @Getter
  private static final Lock lock = new ReentrantLock();

  // 仓库满的条件变量
  @Getter
  private static final Condition full = lock.newCondition();

  // 仓库空的条件变量
  @Getter
  private static final Condition empty = lock.newCondition();

}
