package com.bentonian.framework.mesh;

import java.util.Iterator;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M3dPair;
import com.bentonian.framework.math.MathUtil;
import com.google.common.collect.Sets;

public class MeshEdge extends M3dPair {

  private Set<MeshFace> sharedFaces = null;

  public MeshEdge(MeshVertex A, MeshVertex B) {
    super(A, B);
  }

  @Override
  public MeshVertex getA() {
    return (MeshVertex) A;
  }

  @Override
  public MeshVertex getB() {
    return (MeshVertex) B;
  }

  public boolean isBoundaryEdge() {
    return getFaces().size() == 1;
  }

  public M3d getMidpoint() {
    return MathUtil.midPt(A, B);
  }

  public MeshFace getFaceAlpha() {
    return getFaces().iterator().next();
  }

  public MeshFace getFaceOmega() {
    Iterator<MeshFace> iter = getFaces().iterator();
    iter.next();
    return iter.hasNext() ? iter.next() : null;
  }

  public MeshFace getOtherFace(MeshFace notThisOne) {
    MeshFace alpha = getFaceAlpha();
    MeshFace omega = getFaceOmega();
    return notThisOne.equals(alpha) ? omega : alpha;
  }
  
  public boolean isAttachedToFace(MeshFace face) {
    return getFaceAlpha() == face || getFaceOmega() == face;
  }

  public MeshVertex getOtherVertex(MeshVertex notThisOne) {
    return notThisOne.equals(getA()) ? getB() : getA();
  }

  private Set<MeshFace> getFaces() {
    if (sharedFaces == null) {
      sharedFaces = Sets.newHashSet();
      for (MeshFace f : getA().getFaces()) {
        if (getB().getFaces().contains(f)) {
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
