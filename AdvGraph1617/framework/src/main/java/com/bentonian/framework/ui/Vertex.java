package com.bentonian.framework.ui;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.texture.TexCoord;

public class Vertex extends M3d {

  private static final M3d NONE = new M3d(0, 0, 0);
  private static final M3d BLACK = new M3d(0, 0, 0);

  private M3d normal = NONE;
  private M3d color = BLACK;
  private TexCoord tc = null;

  public Vertex() {
  }

  public Vertex(M3d src) {
    super(src);
  }

  public Vertex(double[] coords) {
    super(coords);
  }

  public Vertex(Vertex src) {
    super(src);
    this.color = src.color;
    this.normal = src.normal;
    this.tc = src.tc;
  }

  public Vertex(double x, double y, double z) {
    super(x, y, z);
  }

  public void setColor(M3d color) {
    this.color = color;
  }

  public M3d getColor() {
    return color;
  }

  public void setNormal(M3d normal) {
    this.normal = normal;
  }

  public M3d getNormal() {
    return normal;
  }

  public Vertex setTextureCoords(TexCoord tc) {
    this.tc = tc;
    return this;
  }

  public TexCoord getTexCoords() {
    return tc;
  }
}
