package com.bentonian.framework.mesh.implicits;

import static java.lang.Math.atan2;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;

/**
 * Demi metaTorus.
 * Inspire by {@link http://www.georgehart.com/bagel/bagel.html}.
 * 
 * @author Alex Benton
 */
public class DemiMetaTorus extends MetaTorus {
  
  private static final M3d Y = new M3d(0, 1, 0);

  public DemiMetaTorus(double strength) {
    super(strength);
  }

  @Override
  public double F(M3d v) {
    M3d inPlane = new M3d(v.getX(), 0, v.getZ());
    M3d onTorus = inPlane.normalized();
    double alpha = atan2(onTorus.getX(), onTorus.getZ());
    M3d toV = v.minus(onTorus.times(R));
    M3d towardsV = toV.normalized();
    M3d tangent = Y.cross(onTorus);
    M3d planeNormal = M4x4.rotationMatrix(tangent, alpha).times(new M3d(0,1,0));
    double dot = towardsV.dot(planeNormal);
    if ((dot > 0) && inPlane.length() >= 0.01) {
      if (toV.length() <= r) {
        double distToPlane = dot * toV.length();
        return 1.0 / (distToPlane + 1) * (distToPlane + 1);
      } else {
        return 0;
      }
    } else {
      return super.F(v);
    }
  }
}
