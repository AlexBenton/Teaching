package com.bentonian.framework.math;

public class LineSegment {

  private final Ray ray;
  private final double lineSegmentLength;

  public LineSegment(Vec3 alpha, Vec3 beta) {
    Vec3 lineSegment = beta.minus(alpha);
    this.lineSegmentLength = lineSegment.length();
    this.ray = new Ray(alpha, lineSegment.times(1 / lineSegmentLength));
  }

  public boolean intersectsCube(Vec3 boxMin, Vec3 boxMax) {
    Double t = ray.intersectsCubePrecisely(boxMin, boxMax);
    return ((t != null) && (ray.at(t).minus(ray.origin).length() <= lineSegmentLength));
  }

  public boolean intersectsTriangle(Vec3 a, Vec3 b, Vec3 c, Vec3 n) {
    Double t = ray.intersectsTriangle(a, b, c, n);
    return ((t != null) && (ray.at(t).minus(ray.origin).length() <= lineSegmentLength));
  }
}
