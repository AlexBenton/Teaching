package com.bentonian.framework.material;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.scene.Primitive;

public abstract class MaterialPrimitive extends Primitive {

  private Material material = new Material();

  protected MaterialPrimitive() {
  }

  protected MaterialPrimitive(M3d color) {
    material.setColor(color);
  }

  public Material getMaterial() {
    return material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public M3d getColor() {
    return material.getColor();
  }

  public MaterialPrimitive setColor(M3d color) {
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
