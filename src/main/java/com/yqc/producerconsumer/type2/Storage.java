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
import lombok.Getter;

public class Storage {

  // 仓库最大存储量
  @Getter
  private final int MAX_SIZE = 100;

  // 仓库存储的载体
  @Getter
  private LinkedList<Object> list = new LinkedList<>();
}