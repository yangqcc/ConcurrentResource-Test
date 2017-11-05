package com.yqc.basic.aqs;

import java.util.ArrayList;
import java.util.List;

public class BaseBoundedBuffer<V> {

  private final List<V> buf;
  private int tail;
  private int head;
  private int count;

  protected BaseBoundedBuffer(int capacity) {
    this.buf = new ArrayList<>(capacity);
  }

  protected synchronized final void doPut(V v) {
    buf.set(tail, v);
    if (++tail == buf.size()) {
      tail = 0;
    }
    ++count;
  }

  protected synchronized final V doTake() {
    V v = buf.get(head);
    if (++head == buf.size()) {
      head = 0;
    }
    --count;
    return v;
  }

  public synchronized final boolean isFull() {
    return count == buf.size();
  }

  public synchronized final boolean isEmpty() {
    return count == 0;
  }
}
