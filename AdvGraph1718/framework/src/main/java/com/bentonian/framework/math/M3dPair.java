package com.bentonian.framework.math;


public class M3dPair {

  protected M3d A, B;
  
  public M3dPair(M3d A, M3d B) {
    this.A = A;
    this.B = B;
  }
  
  public M3d getA() {
    return A;
  }

  public M3d getB() {
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

