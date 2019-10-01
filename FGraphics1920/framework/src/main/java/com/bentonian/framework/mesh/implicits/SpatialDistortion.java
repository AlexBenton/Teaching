package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.math.Vec3;

public abstract class SpatialDistortion implements Force, HasColor {
  
  private final Force innerForce;
  
  public SpatialDistortion(Force innerForce) {
    this.innerForce = innerForce;
  }

  @Override
  public double F(Vec3 v) {
    return innerForce.F(spatialDistortion(v));
  }

  @Override
  public Vec3 getColor() {
    return (innerForce instanceof HasColor) ? ((HasColor) innerForce).getColor() : new Vec3(1,1,1);
  }
  
  protected abstract Vec3 spatialDistortion(Vec3 realSpace);
}
