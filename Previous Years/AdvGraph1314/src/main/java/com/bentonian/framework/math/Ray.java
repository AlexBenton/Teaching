package com.bentonian.framework.math;

import static java.lang.Math.min;

import com.google.common.base.Objects;


public class Ray {

  private static final M3d X = new M3d(1, 0, 0);
  private static final M3d Y = new M3d(0, 1, 0);
  private static final M3d Z = new M3d(0, 0, 1);

  public M3d origin;
  public M3d direction;

  public Ray(M3d origin, M3d direction) {
    this.origin = origin;
    this.direction = direction;
  }

  public Ray transformedBy(M4x4 pointTransform, M4x4 directionTransform) {
    return new Ray(pointTransform.times(origin), directionTransform.times(direction).normalized());
  }

  public M3d at(double t) {
    return origin.plus(direction.times(t));
  }

  public Double intersectPlane(M3d Q, M3d N) {
    double nDotD = N.dot(direction);
    if (Math.abs(nDotD) >= MathConstants.EPSILON) {
      double t = N.dot(Q.minus(origin)) / nDotD;
      return (t >= 0) ? t : null;
    } else {
      return null;
    }
  }

  public boolean intersectsCube(M3d min, M3d max) {
    return intersectsCube(min, max, false) != null;
  }
  
  public Double intersectsCubePrecisely(M3d min, M3d max) {
    return intersectsCube(min, max, true);
  }
  
  public Double intersectsCube(M3d min, M3d max, boolean precise) {
    Double t, bestT = null;

    if (((t = intersectPlane(min, X)) != null) && inBoxYZ(at(t), min, max)) {
      if (precise) {
        return t;
      } else {
        bestT = min(t, Objects.firstNonNull(bestT, t));
      }
    }
    if (((t = intersectPlane(max, X)) != null) && inBoxYZ(at(t), min, max)) {
      if (precise) {
        return t;
      } else {
        bestT = min(t, Objects.firstNonNull(bestT, t));
      }
    }
    if (((t = intersectPlane(min, Y)) != null) && inBoxXZ(at(t), min, max)) {
      if (precise) {
        return t;
      } else {
        bestT = min(t, Objects.firstNonNull(bestT, t));
      }
    }
    if (((t = intersectPlane(max, Y)) != null) && inBoxXZ(at(t), min, max)) {
      if (precise) {
        return t;
      } else {
        bestT = min(t, Objects.firstNonNull(bestT, t));
      }
    }
    if (((t = intersectPlane(min, Z)) != null) && inBoxXY(at(t), min, max)) {
      if (precise) {
        return t;
      } else {
        bestT = min(t, Objects.firstNonNull(bestT, t));
      }
    }
    if (((t = intersectPlane(max, Z)) != null) && inBoxXY(at(t), min, max)) {
      if (precise) {
        return t;
      } else {
        bestT = min(t, Objects.firstNonNull(bestT, t));
      }
    }
    return bestT;
  }

  private boolean inBoxXY(M3d pt, M3d min, M3d max) {
    return ((pt.getY() >= min.getY()) && (pt.getY() <= max.getY())
        && (pt.getX() >= min.getX()) && (pt.getX() <= max.getX()));
  }

  private boolean inBoxXZ(M3d pt, M3d min, M3d max) {
    return ((pt.getX() >= min.getX()) && (pt.getX() <= max.getX())
        && (pt.getZ() >= min.getZ()) && (pt.getZ() <= max.getZ()));
  }

  private boolean inBoxYZ(M3d pt, M3d min, M3d max) {
    return ((pt.getY() >= min.getY()) && (pt.getY() <= max.getY())
        && (pt.getZ() >= min.getZ()) && (pt.getZ() <= max.getZ()));
  }

  public Double intersectsTriangle(M3d a, M3d b, M3d c, M3d n) {
    Double hit = intersectPlane(a, n);
    if (hit != null) {
      M3d P = at(hit);
      double ab = n.cross(b.minus(a)).dot(P.minus(a));
      double bc = n.cross(c.minus(b)).dot(P.minus(b));
      double ca = n.cross(a.minus(c)).dot(P.minus(c));
      return ((ab >= 0) && (bc >= 0) && (ca >= 0)) ? hit : null;
    } else {
      return null;
    }
  }

  @Override
  public String toString() {
    return "[O=[" + origin.toString() + "], D=[" + direction.toString() + "]]";
  }

  @Override
  public boolean equals(Object r) {
    return (r instanceof Ray) 
        ? ((Ray)r).direction.equals(direction) && ((Ray)r).origin.equals(origin) 
        : false;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(direction, origin);
  }
}
