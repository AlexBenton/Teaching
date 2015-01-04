package com.bentonian.framework.mesh.metaballs;

import com.bentonian.framework.math.M3d;

public class MetaTorus implements Force {

  protected static final double R = 4;         // Major radius
  protected static final double r = 1;         // Minor radius
  
  protected final double strength;
  
  public MetaTorus(double strength) {
    this.strength = strength;
  }

  @Override
  public double F(M3d v) {
    M3d inPlane = new M3d(v.getX(), 0, v.getZ());
    if (inPlane.length() < 0.01) {
      return 0;
    } else {
      M3d projectedToTorusCenterline = inPlane.normalized().times(R);
      return MetaBall.wyvill(v.minus(projectedToTorusCenterline).length(), strength);
    }
  }
}
