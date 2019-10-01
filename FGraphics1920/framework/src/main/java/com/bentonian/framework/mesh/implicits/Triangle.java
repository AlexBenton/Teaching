package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.ui.Vertex;

public class Triangle {

  public Vertex a, b, c;
  public Vec3 normal;
  
  public Triangle(Vertex a, Vertex b, Vertex c, Vec3 normal) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.normal = normal;
  }
  
  @Override
  public String toString() {
    return "[" + a + "], [" + b + "], [" + c + "]";
  }
}
