package com.bentonian.framework.math;

import static com.bentonian.framework.math.MathConstants.EPSILON;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class Vec3 {
  
  double vec[] = new double[3];  
  
  public Vec3() {
    vec[0] = vec[1] = vec[2] = 0;
  }
  
  public Vec3(double d) {
    vec[0] = vec[1] = vec[2] = d;
  }
  
  public Vec3(double x, double y, double z) {
    set(x, y, z);
  }
  
  public static Vec3 fromRGBA(int rgba) {
    int r = (rgba >> 16) & 0xFF;
    int g = (rgba >> 8) & 0xFF;
    int b = (rgba >> 0) & 0xFF;
    return new Vec3(r, g, b).times(1.0 / 255.0);
  }
  
  public Vec3(Vec3 A) {
    set(A);
  }

  public Vec3(double[] data) {
    set(data);
  }
  
  public Vec3(int[] data) {
    set(data[0], data[1], data[2]);
  }
  
  public void set(double x, double y, double z) {
    vec[0] = x;
    vec[1] = y;
    vec[2] = z;
  }
  
  public void set(double v) {
    vec[0] = vec[1] = vec[2] = v;
  }
  
  public void set(Vec3 A) {
    vec[0] = A.vec[0];
    vec[1] = A.vec[1];
    vec[2] = A.vec[2];
  }

  public void set(double[] data) {
    vec[0] = data[0];
    vec[1] = data[1];
    vec[2] = data[2];
  }

  public Vec3 neg() {
    return new Vec3(-vec[0], -vec[1], -vec[2]);
  }

  public Vec3 plus(Vec3 B) {
    return new Vec3(vec[0] + B.vec[0], vec[1] + B.vec[1], vec[2] + B.vec[2]); 
  }

  public Vec3 minus(Vec3 B) {
    return new Vec3(vec[0] - B.vec[0], vec[1] - B.vec[1], vec[2] - B.vec[2]); 
  }

  public Vec3 times(double k) {
    return new Vec3(k*vec[0], k*vec[1], k*vec[2]);
  }

  public Vec3 cross(Vec3 B) {
    return new Vec3(vec[1]*B.vec[2] - vec[2]*B.vec[1], 
        vec[2]*B.vec[0] - vec[0]*B.vec[2],
        vec[0]*B.vec[1] - vec[1]*B.vec[0]);
  }

  public Vec3 lerp(Vec3 B, double t) {
    return new Vec3(vec[0]+t*(B.vec[0]-vec[0]),
        vec[1]+t*(B.vec[1]-vec[1]),
        vec[2]+t*(B.vec[2]-vec[2]));
  }

  public Vec3 normalized() {
    double len = length();
    
    if (len > EPSILON) {
      return this.times(1.0 / len);
    } else {
      return new Vec3(0,0,0);
    }
  }

  public double get(int i) {
    return vec[i];
  }

  public double[] get() { 
    return vec;
  }
  
  public float[] asFloats() {
    float[] array = { (float) vec[0], (float) vec[1], (float) vec[2] };
    return array;
  }
  
  public int asRGBA() {
    int r = max(min((int) (vec[0] * 255), 255), 0);
    int g = max(min((int) (vec[1] * 255), 255), 0);
    int b = max(min((int) (vec[2] * 255), 255), 0);
    return (0xFF << 24) | (r << 16) | (g << 8) | (b << 0);
  }
  
  public void set(int i, double val) {
    vec[i] = val;
  }

  public double getX() {
    return vec[0];
  }

  public void setX(double val) {
    vec[0] = val;
  }

  public double getY() {
    return vec[1];
  }

  public void setY(double val) {
    vec[1] = val;
  }

  public double getZ() {
    return vec[2];
  }

  public void setZ(double val) {
    vec[2] = val;
  }

  public double dot(Vec3 A) {
    return vec[0]*A.vec[0] + vec[1]*A.vec[1] + vec[2]*A.vec[2];
  }

  public boolean cmp(Vec3 A, double epsilon) {
    return
      (Math.abs(vec[0]-A.vec[0])<epsilon) &&
      (Math.abs(vec[1]-A.vec[1])<epsilon) &&
      (Math.abs(vec[2]-A.vec[2])<epsilon);
  }

  public double length() {
    return (double) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
  }

  public double lengthSquared() {
    return vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2];
  }
  
  public boolean lessThan(Vec3 A) {
    return ((getX() < A.getX()) || 
            (getX() == A.getX() && getY() < A.getY()) || 
            (getX() == A.getX() && getY() == A.getY() && getZ() < A.getZ()));
  }
  
  public Vec3 toAxis() {
    double x = Math.abs(getX());
    double y = Math.abs(getY());
    double z = Math.abs(getZ());
    
    if (x >= y && x >= z) {
      return new Vec3(getX() < 0 ? -1 : 1, 0, 0);
    } else if (y >= x && y >= z) {
      return new Vec3(0, getY() < 0 ? -1 : 1, 0);
    } else if (z >= x && z >= y) {
      return new Vec3(0, 0, getZ() < 0 ? -1 : 1);
    } else {
      return MathConstants.ORIGIN;
    }
  }
  
  @Override
  public String toString() {
    return getX() + ", " + getY() + ", " + getZ();
  }

  @Override
  public boolean equals(Object v) {
    if (v instanceof Vec3) {
      Vec3 other = (Vec3) v;
      return ((long) (vec[0] * 1e7)) == ((long) (other.vec[0] * 1e7))
          && ((long) (vec[1] * 1e7)) == ((long) (other.vec[1] * 1e7))
          && ((long) (vec[2] * 1e7)) == ((long) (other.vec[2] * 1e7));
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    long hash = (991 * ((long) (vec[0] * 1e7))) 
        ^ (997 * ((long) (vec[1] * 1e7)))
        ^ (1009 * ((long) (vec[2] * 1e7)));
    return (int) ((hash >> 32) ^ (hash & 0xFFFFFFFF));
  }
}
