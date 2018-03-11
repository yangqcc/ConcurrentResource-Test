package com.yqc.producerconsumer.type4;

import util.concurrent.ArrayBlockingQueue;
import lombok.Getter;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */
public class Storage {

  @Getter
  private final int MAX_NUM = 100;

  @Getter
  private final ArrayBlockingQueue<Object> queue = new ArrayBlockingQueue<>(MAX_NUM);

}
