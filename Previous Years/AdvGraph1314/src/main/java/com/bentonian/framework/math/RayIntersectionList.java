package com.bentonian.framework.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RayIntersectionList extends ArrayList<RayIntersection> {

  private static final Comparator<RayIntersection> COMPARE_INTERSECTION = 
      new Comparator<RayIntersection>() {
        @Override
        public int compare(RayIntersection A, RayIntersection B) {
          double dt = A.t - B.t;
          return (dt > MathConstants.EPSILON) ? 1 : (dt < -MathConstants.EPSILON) ? -1 : 0;
        }
      };

  public RayIntersectionList(List<RayIntersection> hits) {
    addAll(hits);
    Collections.sort(this, COMPARE_INTERSECTION);
  }

  public RayIntersection getHead() {
    return get(0);
  }

  public double peek() {
    return get(0).t;
  }

  public RayIntersection pop() {
    RayIntersection head = getHead();
    remove(0);
    return head;
  }
}
