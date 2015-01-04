package com.bentonian.framework.mesh.metaballs;

import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.math.M3d;

public abstract class SpatialDistortion implements Force, HasColor {
  
  private final Force innerForce;
  
  public SpatialDistortion(Force innerForce) {
    this.innerForce = innerForce;
  }

  @Override
  public double F(M3d v) {
    return innerForce.F(spatialDistortion(v));
  }

  @Override
  public M3d getColor() {
    return (innerForce instanceof HasColor) ? ((HasColor) innerForce).getColor() : new M3d(1,1,1);
  }
  
  protected abstract M3d spatialDistortion(M3d realSpace);
}
