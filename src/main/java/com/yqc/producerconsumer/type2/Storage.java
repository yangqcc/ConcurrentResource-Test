package com.yqc.producerconsumer.type2;

/**
 * <p>title:</p>
 * <p>description:</p>
 *
 * @author yangqc
 * @date Created in 2018-03-10
 * @modified By yangqc
 */

import java.util.LinkedList;

public class Storage {

  // 仓库最大存储量
  private final int MAX_SIZE = 100;

  // 仓库存储的载体
  private LinkedList<Object> list = new LinkedList<>();

  public LinkedList<Object> getList() {
    return list;
  }

  public int getMAX_SIZE() {
    return MAX_SIZE;
  }
}