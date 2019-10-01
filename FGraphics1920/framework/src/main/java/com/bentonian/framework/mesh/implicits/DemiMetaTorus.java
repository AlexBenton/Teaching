package com.bentonian.framework.mesh.implicits;

import static java.lang.Math.atan2;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.math.M4x4;

/**
 * Demi metaTorus.
 * Inspire by {@link http://www.georgehart.com/bagel/bagel.html}.
 * 
 * @author Alex Benton
 */
public class DemiMetaTorus extends MetaTorus {
  
  private static final Vec3 Y = new Vec3(0, 1, 0);

  public DemiMetaTorus(double strength) {
    super(strength);
  }

  @Override
  public double F(Vec3 v) {
    Vec3 inPlane = new Vec3(v.getX(), 0, v.getZ());
    Vec3 onTorus = inPlane.normalized();
    double alpha = atan2(onTorus.getX(), onTorus.getZ());
    Vec3 toV = v.minus(onTorus.times(R));
    Vec3 towardsV = toV.normalized();
    Vec3 tangent = Y.cross(onTorus);
    Vec3 planeNormal = M4x4.rotationMatrix(tangent, alpha).times(new Vec3(0,1,0));
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
