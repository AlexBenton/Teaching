package com.bentonian.framework.mesh.implicits;

import static java.lang.Math.min;
import static java.lang.Math.pow;

import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;

public class MetaCube implements Force, HasColor {

  private static final double MAX = 10000;
  private static final M3d PINK = new M3d(205, 92, 92).times(1.0 / 255.0);
  
  @Override
  public double F(M3d v) {
    double x = Math.abs(v.getX());
    double y = Math.abs(v.getY());
    double z = Math.abs(v.getZ());

    // A force function that drops off continuously, instead of being a sawtooth, allows blending 
    x = (x < MathConstants.EPSILON) ? MAX : min(MAX, pow(1 / x, 10));
    y = (y < MathConstants.EPSILON) ? MAX : min(MAX, pow(1 / y, 10));
    z = (z < MathConstants.EPSILON) ? MAX : min(MAX, pow(1 / z, 10));
    return min(x, min(y,  z));
  }

  @Override
  public M3d getColor() {
    return PINK;
  }
}
