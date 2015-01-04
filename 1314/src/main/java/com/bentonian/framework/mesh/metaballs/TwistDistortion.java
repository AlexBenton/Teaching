package com.bentonian.framework.mesh.metaballs;

import com.bentonian.framework.math.M3d;

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
  protected M3d spatialDistortion(M3d realSpace) {
    double t = twist * realSpace.getX() / 1.5;
    return new M3d(
        realSpace.getX(), 
        realSpace.getY() * Math.cos(t) - realSpace.getZ() * Math.sin(t), 
        realSpace.getY() * Math.sin(t) + realSpace.getZ() * Math.cos(t));
  }
}
