package com.bentonian.framework.mesh.implicits;

import java.util.Optional;

import com.bentonian.framework.math.Vec3;

public class Sample extends Vec3 {
  private final double force;
  private final Optional<Vec3> color;
  private final Optional<Vec3> normal;
  
  public Sample(Vec3 position, double force) {
    super(position);
    this.force = force; 
    this.color = Optional.empty();
    this.normal = Optional.empty();
  }
  
  public Sample(Vec3 position, double force, Vec3 color) {
    super(position);
    this.force = force; 
    this.color = Optional.of(color);
    this.normal = Optional.empty();
  }
  
  public Sample(Vec3 position, Vec3 normal, double force) {
    super(position);
    this.force = force; 
    this.color = Optional.empty();
    this.normal = Optional.of(normal);
  }
  
  public double getForce() {
    return force;
  }
  
  public Optional<Vec3> getColor() {
    return color;
  }
  
  public Optional<Vec3> getNormal() {
    return normal;
  }
}