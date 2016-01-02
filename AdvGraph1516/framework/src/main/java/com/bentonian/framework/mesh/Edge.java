package com.bentonian.framework.mesh;

import java.util.Iterator;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M3dPair;
import com.google.common.collect.Sets;

public class Edge extends M3dPair {

  private Set<Face> sharedFaces = null;

  public Edge(Vertex A, Vertex B) {
    super(A, B);
  }

  @Override
  public Vertex getA() {
    return (Vertex) A;
  }

  @Override
  public Vertex getB() {
    return (Vertex) B;
  }

  public boolean isBoundaryEdge() {
    return getFaces().size() == 1;
  }

  public M3d getMidpoint() {
    return A.plus(B).times(0.5);
  }

  public Face getFaceAlpha() {
    return getFaces().iterator().next();
  }

  public Face getFaceOmega() {
    Iterator<Face> iter = getFaces().iterator();
    iter.next();
    return iter.hasNext() ? iter.next() : null;
  }

  public Face getOtherFace(Face notThisOne) {
    Face alpha = getFaceAlpha();
    Face omega = getFaceOmega();
    return notThisOne.equals(alpha) ? omega : alpha;
  }
  
  public boolean isAttachedToFace(Face face) {
    return getFaceAlpha() == face || getFaceOmega() == face;
  }

  public Vertex getOtherVertex(Vertex notThisOne) {
    return notThisOne.equals(getA()) ? getB() : getA();
  }

  private Set<Face> getFaces() {
    if (sharedFaces == null) {
      sharedFaces = Sets.newHashSet();
      for (Face f : ((Vertex) A).getFaces()) {
        if (((Vertex) B).getFaces().contains(f)) {
          sharedFaces.add(f);
        }
      }
      if (sharedFaces.isEmpty()) {
        throw new RuntimeException("Error in data, edge has no faces.");
      }
    }
    return sharedFaces;
  }
}
