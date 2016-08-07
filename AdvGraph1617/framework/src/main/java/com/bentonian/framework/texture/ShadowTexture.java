package com.bentonian.framework.texture;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.scene.Primitive;


public class ShadowTexture extends BufferedProceduralImageTexture {

  private static final double OUTLINE_THICKNESS = 0.05;

  private final Primitive shadowSource;
  private final Primitive shadowTarget;
  private final Texture baseTexture;

  private final M3d shadowDirectionWorldCoords;
  private final M3d axisA, axisB;
  private final M3d color;

  private boolean outlineOnly = false;

  /**
   * @param shadowSource Object which is casting the shadow
   * @param shadowTarget Object which is receiving the shadow
   * @param baseTexture Default texture map
   * @param shadowDirectionWorldCoords Vector *from* shadowed object *towards* shadow-casting object.  (Opposite of light direction.)
   * @param color Shadow color to blend into base texture
   */
  public ShadowTexture(
      Primitive shadowSource, 
      Primitive shadowTarget, 
      Texture baseTexture, 
      M3d shadowDirectionWorldCoords, 
      M3d color) {
    this.shadowSource = shadowSource;
    this.shadowTarget = shadowTarget;
    this.baseTexture = baseTexture;
    this.shadowDirectionWorldCoords = shadowDirectionWorldCoords;
    this.color = color;
    M3d swizzled = (Math.abs(shadowDirectionWorldCoords.getX()) < 0.9) 
        ? MathConstants.X_AXIS : MathConstants.Y_AXIS; 
    this.axisA = shadowDirectionWorldCoords.cross(swizzled).normalized();
    this.axisB = shadowDirectionWorldCoords.cross(axisA).normalized();
  }

  /**
   * Caution: expensive!
   */
  public ShadowTexture enableOutlineOnly() {
    this.outlineOnly = true;
    return this;
  }

  @Override
  public M3d getColor(IsTextured target, M3d pt) {
    M3d ptInWorldsCoords = getInWorldCoords(pt);
    if (isShadowed(ptInWorldsCoords) 
        && (!outlineOnly || isOnOutlineEdge(ptInWorldsCoords))) {
      return color;
    }
    return baseTexture.getColor(target, pt);
  }

  boolean isOnOutlineEdge(M3d ptInWorldCoords) {
    for (int i = 0; i < 8; i++) {
      double t = Math.PI * i / 4.0;
      if (!isShadowed(ptInWorldCoords
          .plus(axisA.times(Math.cos(t) * OUTLINE_THICKNESS))
          .plus(axisB.times(Math.sin(t) * OUTLINE_THICKNESS)))) {
        return true;
      }
    }
    return false;
  }

  boolean isShadowed(M3d ptInWorldCoords) {
    Ray ray = new Ray(ptInWorldCoords, shadowDirectionWorldCoords);
    RayIntersections hits = RayTracerEngine.traceScene(shadowSource, ray);
    return !hits.isEmpty();
  }
  
  M3d getInWorldCoords(M3d pt) {
    return shadowTarget.getLocalToParent().times(pt);
  }

  @Override
  protected int getBufferedImageWidth() {
    return (baseTexture instanceof BufferedImageTexture)
        ? ((BufferedImageTexture) baseTexture).getBufferedImage().getWidth()
        : super.getBufferedImageWidth();
  }

  @Override
  protected int getBufferedImageHeight() {
    return (baseTexture instanceof BufferedImageTexture)
        ? ((BufferedImageTexture) baseTexture).getBufferedImage().getHeight()
        : super.getBufferedImageHeight();
  }
}
