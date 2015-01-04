package com.bentonian.framework.raytrace.csg;

import java.util.Set;

import com.bentonian.framework.scene.Primitive;
import com.google.common.collect.ImmutableSet;


public class Intersection extends CsgBoolean {

  public Intersection(Primitive A, Primitive B) {
    super(A,B);
  }

  @Override
  protected Set<CsgState> getStatesOfInterest() {
    return ImmutableSet.of(CsgState.InBoth);
  }
}
