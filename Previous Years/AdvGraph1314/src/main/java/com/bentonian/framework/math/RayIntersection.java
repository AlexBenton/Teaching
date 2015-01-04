package com.bentonian.framework.math;

import com.bentonian.framework.material.Material;

public class RayIntersection {
  
  public double t;
  public M3d point;
  public M3d normal;
  public Material material;

  public RayIntersection(double t, M3d point, M3d normal, Material material) {
    this.t = t;
    this.point = point;
    this.normal = normal;
    this.material = material;
  }

  @Override
  public String toString() {
    return "[" + t + " (" + point.getX() + ", " + point.getY() + ", " + point.getZ() + ")]";
  }
}
