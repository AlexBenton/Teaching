package com.bentonian.framework.mesh.textures;

public class TexCoord {

  public final double u, v;
  
  public TexCoord(double u, double v) {
    this.u = u;
    this.v = v;
  }
  
  public float[] asFloats() {
    return new float[] { (float) u, (float) v };
  }
}
