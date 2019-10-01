package com.bentonian.framework.math;

import java.util.ArrayList;

public class MatrixStack extends ArrayList<M4x4> {

  public MatrixStack() {
    add(M4x4.identity());
  }

  public void push(M4x4 T) {
    add(peek().times(T));
  }

  public void pushReversed(M4x4 T) {
    add(isEmpty() ? new M4x4(T) : T.times(peek()));
  }

  public void pop() {
    if (!isEmpty()) {
      remove(size() - 1);
    }
  }
  
  public M4x4 peek() {
    return isEmpty() ? M4x4.identity() : get(size() - 1);
  }
}
