package com.bentonian.framework.math;

public class LineSegment {

  private final Ray ray;
  private final double lineSegmentLength;

  public LineSegment(M3d alpha, M3d beta) {
    M3d lineSegment = beta.minus(alpha);
    this.lineSegmentLength = lineSegment.length();
    this.ray = new Ray(alpha, lineSegment.times(1 / lineSegmentLength));
  }

  public boolean intersectsCube(M3d boxMin, M3d boxMax) {
    Double t = ray.intersectsCubePrecisely(boxMin, boxMax);
    return ((t != null) && (ray.at(t).minus(ray.origin).length() <= lineSegmentLength));
  }

  public boolean intersectsTriangle(M3d a, M3d b, M3d c, M3d n) {
    Double t = ray.intersectsTriangle(a, b, c, n);
    return ((t != null) && (ray.at(t).minus(ray.origin).length() <= lineSegmentLength));
  }
}
