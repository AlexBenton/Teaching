package com.bentonian.framework.math;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.scene.Primitive;
import com.google.common.collect.Lists;


public class RayIntersections implements Iterable<RayIntersection> {

  private List<RayIntersection> hits = Lists.newLinkedList();

  public RayIntersections add(@Nullable Primitive primitive, double t, M3d point, M3d normal, Material material) {
    if (t >= MathConstants.EPSILON) {
      add(new RayIntersection(primitive, t, point, normal, material));
    }
    return this;
  }

  public void addAll(RayIntersections source) {
    if (source != null) {
      hits.addAll(source.hits);
    }
  }

  public void add(RayIntersection toAdd) {
    hits.add(toAdd);
  }

  public RayIntersection getNearest() {
    RayIntersection nearest = null;
    for (RayIntersection hit : hits) {
      if ((nearest == null) || (hit.t < nearest.t)) {
        nearest = hit;
      }
    }
    return nearest;
  }

  public RayIntersectionList sorted() {
    return new RayIntersectionList(hits);
  }

  public boolean isEmpty() {
    return hits.isEmpty();
  }

  @Override
  public Iterator<RayIntersection> iterator() {
    return hits.iterator();
  }
}
