package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.math.M3d;

class Sample extends M3d {
  double force;
  M3d color;
  
  public Sample(M3d position, double force, M3d color) {
    super(position);
    this.force = force; 
    this.color = color;
  }
}