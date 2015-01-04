package com.bentonian.framework.mesh;

import java.util.LinkedList;

import com.bentonian.framework.math.M3d;

public class SimpleMesh extends LinkedList<SimpleFace> {

  private SimpleFace addPoly(SimpleVertex a, SimpleVertex b, SimpleVertex c, M3d normal) {
    SimpleFace poly = new SimpleFace(a, b, c, normal);
    add(poly);
    return poly;
  }

  public SimpleFace addPolyWithOrientation(SimpleVertex a, SimpleVertex b, SimpleVertex c, M3d normal) {
    M3d n = c.minus(b).cross(a.minus(b)).normalized();

    if (n.dot(normal) > 0) {
      return addPoly(a, b, c, n);
    } else {
      return addPoly(a, c, b, n.neg());
    }
  }

  public void addMesh(SimpleMesh mesh) {
    for (SimpleFace poly : mesh) {
      add(poly);
    }
  }
}
