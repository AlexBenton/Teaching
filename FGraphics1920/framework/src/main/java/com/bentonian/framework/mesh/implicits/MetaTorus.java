package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.math.Vec3;

public class MetaTorus implements Force {

  protected static final double R = 4;         // Major radius
  protected static final double r = 1;         // Minor radius
  
  protected final double strength;
  
  public MetaTorus(double strength) {
    this.strength = strength;
  }

  @Override
  public double F(Vec3 v) {
    Vec3 inPlane = new Vec3(v.getX(), 0, v.getZ());
    if (inPlane.length() < 0.01) {
      return 0;
    } else {
      Vec3 projectedToTorusCenterline = inPlane.normalized().times(R);
      return MetaBall.wyvill(v.minus(projectedToTorusCenterline).length(), strength);
    }
  }
}
