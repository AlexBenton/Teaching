package com.bentonian.framework.math;


public class M3dPair {

  protected Vec3 A, B;
  
  public M3dPair(Vec3 A, Vec3 B) {
    this.A = A;
    this.B = B;
  }
  
  public Vec3 getA() {
    return A;
  }

  public Vec3 getB() {
    return B;
  }

  @Override
  public int hashCode() {
    return A.hashCode() ^ B.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof M3dPair) {
      M3dPair pair = (M3dPair) obj;
      return (A.equals(pair.A) && B.equals(pair.B)) || (A.equals(pair.B) && B.equals(pair.A));
    } else {
      return false;
    }
  }
}

