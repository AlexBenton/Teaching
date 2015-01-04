package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;

public class MetaStrip extends MetaCube {
  
  private final M4x4 squash;

  public MetaStrip(M3d scale) {
    this.squash = M4x4.scaleMatrix(scale);
  }
  
  @Override
  public double F(M3d v) {
    return super.F(squash.times(v));
  }
}
