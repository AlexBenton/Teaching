package com.bentonian.raytrace.csg;

import java.util.Set;

import com.bentonian.framework.math.RayIntersection;
import com.bentonian.framework.math.RayIntersectionList;
import com.bentonian.framework.scene.Primitive;
import com.google.common.collect.ImmutableSet;


public class Difference extends CsgBoolean {

  public Difference(Primitive A, Primitive B) {
    super(A,B);
  }
  
  @Override
  protected RayIntersection popB(RayIntersectionList hitsB) {
    RayIntersection hit = hitsB.pop();
    return new RayIntersection(hit.t, hit.point, hit.normal.times(-1), hit.material);
  }

  @Override
  protected Set<CsgState> getStatesOfInterest() {
    return ImmutableSet.of(CsgState.InA);
  }
}
