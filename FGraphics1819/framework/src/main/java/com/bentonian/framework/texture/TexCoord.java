package com.bentonian.framework.texture;

public class TexCoord {

  public final double u, v;
  
  public TexCoord(double u, double v) {
    this.u = u;
    this.v = v;
  }
  
  public float[] asFloats() {
    float[] array = { (float) u, (float) v };
    return array;
  }
}
