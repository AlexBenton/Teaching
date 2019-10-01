package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.math.Vec3;

public class TwistDistortion extends SpatialDistortion {

  private double twist = 1;
  
  public TwistDistortion(Force innerForce) {
    super(innerForce);
  }
  
  public double getTwist() {
    return twist;
  }

  public void setTwist(double twist) {
    this.twist = twist;
  }
  
  @Override
  protected Vec3 spatialDistortion(Vec3 realSpace) {
    double t = twist * realSpace.getX() / 1.5;
    return new Vec3(
        realSpace.getX(), 
        realSpace.getY() * Math.cos(t) - realSpace.getZ() * Math.sin(t), 
        realSpace.getY() * Math.sin(t) + realSpace.getZ() * Math.cos(t));
  }
}
