package com.bentonian.framework.mesh;

import com.bentonian.framework.math.M3d;

public class SimpleFace {

  public SimpleVertex a, b, c;
  public M3d normal;
  
  public SimpleFace(SimpleVertex a, SimpleVertex b, SimpleVertex c, M3d normal) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.normal = normal;
  }
}
