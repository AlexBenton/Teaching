package com.bentonian.framework.ui;

import static com.bentonian.framework.material.Colors.BLACK;

import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.texture.TexCoord;

public class Vertex extends Vec3 {

  private Vec3 normal = MathConstants.ORIGIN;
  private Vec3 color = BLACK;
  private TexCoord tc = null;

  public Vertex() {
  }

  public Vertex(Vec3 src) {
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

  public void setColor(Vec3 color) {
    this.color = color;
  }

  public Vec3 getColor() {
    return color;
  }

  public void setNormal(Vec3 normal) {
    this.normal = normal;
  }

  public Vec3 getNormal() {
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
