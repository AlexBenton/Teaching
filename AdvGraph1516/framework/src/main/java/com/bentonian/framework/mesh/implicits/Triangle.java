package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.Vertex;

public class Triangle {

  public Vertex a, b, c;
  public M3d normal;
  
  public Triangle(Vertex a, Vertex b, Vertex c, M3d normal) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.normal = normal;
  }
}
