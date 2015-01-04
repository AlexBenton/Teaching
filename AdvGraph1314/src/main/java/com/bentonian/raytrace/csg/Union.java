package com.bentonian.raytrace.csg;

import java.util.Set;

import com.bentonian.framework.scene.Primitive;
import com.google.common.collect.ImmutableSet;



public class Union extends CsgBoolean {

  public Union(Primitive A, Primitive B) {
    super(A,B);
  }

  @Override
  protected Set<CsgState> getStatesOfInterest() {
    return ImmutableSet.of(CsgState.InA, CsgState.InB, CsgState.InBoth);
  }
}
