package com.bentonian.framework.math;

import static java.lang.Math.min;

import com.google.common.base.Objects;


public class Ray {

  private static final Vec3 X = new Vec3(1, 0, 0);
  private static final Vec3 Y = new Vec3(0, 1, 0);
  private static final Vec3 Z = new Vec3(0, 0, 1);

  public Vec3 origin;
  public Vec3 direction;

  public Ray(Vec3 origin, Vec3 direction) {
    this.origin = origin;
    this.direction = direction;
  }

  public Ray transformedBy(M4x4 pointTransform, M4x4 directionTransform) {
    return new Ray(pointTransform.times(origin), directionTransform.times(direction).normalized());
  }

  public Vec3 at(double t) {
    return origin.plus(direction.times(t));
  }

  public Double intersectPlane(Vec3 Q, Vec3 N) {
    double nDotD = N.dot(direction);
    if (Math.abs(nDotD) >= MathConstants.EPSILON) {
      double t = N.dot(Q.minus(origin)) / nDotD;
      return (t >= 0) ? t : null;
    } else {
      return null;
    }
  }

  public boolean intersectsCube(Vec3 min, Vec3 max) {
    return intersectsCube(min, max, false) != null;
  }
  
  public Double intersectsCubePrecisely(Vec3 min, Vec3 max) {
    return intersectsCube(min, max, true);
  }
  
  public Double intersectsCube(Vec3 min, Vec3 max, boolean precise) {
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

  private boolean inBoxXY(Vec3 pt, Vec3 min, Vec3 max) {
    return ((pt.getY() >= min.getY()) && (pt.getY() <= max.getY())
        && (pt.getX() >= min.getX()) && (pt.getX() <= max.getX()));
  }

  private boolean inBoxXZ(Vec3 pt, Vec3 min, Vec3 max) {
    return ((pt.getX() >= min.getX()) && (pt.getX() <= max.getX())
        && (pt.getZ() >= min.getZ()) && (pt.getZ() <= max.getZ()));
  }

  private boolean inBoxYZ(Vec3 pt, Vec3 min, Vec3 max) {
    return ((pt.getY() >= min.getY()) && (pt.getY() <= max.getY())
        && (pt.getZ() >= min.getZ()) && (pt.getZ() <= max.getZ()));
  }

  public Double intersectsTriangle(Vec3 a, Vec3 b, Vec3 c, Vec3 n) {
    Double hit = intersectPlane(a, n);
    if (hit != null) {
      Vec3 P = at(hit);
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
