package com.bentonian.framework.material;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.scene.Primitive;

public abstract class MaterialPrimitive extends Primitive {

  private Material material = new Material();

  protected MaterialPrimitive() {
  }

  protected MaterialPrimitive(Vec3 color) {
    material.setColor(color);
  }

  public Material getMaterial() {
    return material;
  }

  public Vec3 getColor() {
    return material.getColor();
  }

  public MaterialPrimitive setMaterial(Material material) {
    this.material = material;
    return this;
  }

  public MaterialPrimitive setColor(Vec3 color) {
    material.setColor(color);
    return this;
  }

  public MaterialPrimitive setReflectivity(double reflectivity) {
    material.setReflectivity(reflectivity);
    return this;
  }

  public MaterialPrimitive setRefractiveIndex(double refraction) {
    material.setRefractiveIndex(refraction);
    return this;
  }

  public MaterialPrimitive setTransparency(double transparency) {
    material.setTransparency(transparency);
    return this;
  }

  public MaterialPrimitive setLightingCoefficients(double ka, double kd, double ks, double specularShininess) {
    material.setLightingCoefficients(ka, kd, ks, specularShininess);
    return this;
  }
}
