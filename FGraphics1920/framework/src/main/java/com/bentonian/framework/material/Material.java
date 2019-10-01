package com.bentonian.framework.material;

import com.bentonian.framework.math.Vec3;

public class Material implements HasColor {

  protected Vec3 color;
  protected double reflectivity;
  protected double transparency;
  protected double ka, kd, ks;
  protected double specularShininess;
  protected double refractiveIndex;

  public Material() {
    color = new Vec3(1,1,1);
    reflectivity = 0;
    transparency = 0;
    specularShininess = 1;
    ka = 0.2;
    kd = 0.6;
    ks = 0.2;
    refractiveIndex = 1.0;
  }

  public Material(Material src) {
    this.color = src.color;
    this.reflectivity = src.reflectivity;
    this.specularShininess = src.specularShininess;
    this.transparency = src.transparency;
    this.ka = src.ka;
    this.kd = src.kd;
    this.ks = src.ks;
    this.refractiveIndex = src.refractiveIndex;
  }

  @Override
  public Vec3 getColor() {
    return color;
  }

  public Material setColor(Vec3 color) {
    this.color = color;
    return this;
  }

  public double getReflectivity() {
    return reflectivity;
  }

  public Material setReflectivity(double reflectivity) {
    this.reflectivity = reflectivity;
    return this;
  }

  public double getTransparency() {
    return transparency;
  }

  public Material setTransparency(double transparency) {
    this.transparency = transparency;
    return this;
  }

  public double getSpecularShininess() {
    return specularShininess;
  }

  public Material setSpecularShininess(double shininess) {
    this.specularShininess = shininess;
    return this;
  }

  public double getRefractiveIndex() {
    return refractiveIndex;
  }

  public Material setRefractiveIndex(double refractiveIndex) {
    this.refractiveIndex = refractiveIndex;
    return this;
  }

  public double getKa() {
    return ka;
  }

  public Material setKa(double ka) {
    this.ka = ka;
    return this;
  }

  public double getKd() {
    return kd;
  }

  public Material setKd(double kd) {
    this.kd = kd;
    return this;
  }

  public double getKs() {
    return ks;
  }

  public Material setKs(double ks) {
    this.ks = ks;
    return this;
  }

  public Material setLightingCoefficients(double ka, double kd, double ks, double specularShininess) {
    this.ka = ka;
    this.kd = kd;
    this.ks = ks;
    this.specularShininess = specularShininess;
    return this;
  }
}
