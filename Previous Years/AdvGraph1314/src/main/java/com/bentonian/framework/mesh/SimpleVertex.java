package com.bentonian.framework.mesh;

import com.bentonian.framework.math.M3d;

public class SimpleVertex extends M3d {

  private static final M3d BLACK = new M3d(0, 0, 0);
  
  public M3d color;
  
  public SimpleVertex() {
    this.color = BLACK;
  }
  
  public SimpleVertex(M3d src) {
    super(src);
    this.color = BLACK;
  }
  
  public SimpleVertex(SimpleVertex src) {
    super(src);
    this.color = src.color;
  }
  
  public SimpleVertex(M3d src, M3d color) {
    super(src);
    this.color = color;
  }
  
  public M3d getColor() {
    return color;
  }
  
  public void setColor(M3d color) {
    this.color = color;
  }
}
