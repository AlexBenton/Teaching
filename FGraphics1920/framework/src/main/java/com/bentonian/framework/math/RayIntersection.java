package com.bentonian.framework.math;

import javax.annotation.Nullable;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.scene.Primitive;

public class RayIntersection {

  public Primitive primitive;
  public double t;
  public Vec3 point;
  public Vec3 normal;
  public Material material;

  public RayIntersection(@Nullable Primitive primitive, double t, Vec3 point, Vec3 normal, Material material) {
    this.primitive = primitive;
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
