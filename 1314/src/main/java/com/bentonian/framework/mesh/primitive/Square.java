package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathConstants.EPSILON;
import static java.lang.Math.abs;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.framework.ui.GLVertexData;
import com.google.common.base.Preconditions;

public class Square extends CompiledPrimitive implements IsRayTraceable {

  protected static final M3d Z_AXIS = new M3d(0, 0, 1);
  
  public Square() {
    super(GLVertexData.Mode.QUADS);
  }

  public Square(M3d color) {
    super(GLVertexData.Mode.QUADS, color);
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    double t = -(ray.origin.getZ()) / ray.direction.getZ();
    if (t > MathConstants.EPSILON) {
      M3d pt = ray.at(t);
      Preconditions.checkState(abs(pt.getZ()) < EPSILON);
      if (abs(pt.getX()) <= 1 + EPSILON && abs(pt.getY()) <= 1 + EPSILON) {
        return new RayIntersections().add(t, pt, getNormal(pt), getMaterial(pt));
      }
    }
    return null;
  }

  protected M3d getNormal(M3d pt) {
    return Z_AXIS;
  }

  protected Material getMaterial(M3d pt) {
    return getMaterial();
  }

  @Override
  protected void renderLocal(GLRenderingContext glCanvas) {
    if (!isCompiled()) {
      addFace();
    }
    super.renderLocal(glCanvas);
  }

  protected void addFace() {
    normal(Z_AXIS);
    for (int i = 0; i < 4; i++) {
      vertex(MathConstants.CORNERS_OF_A_SQUARE[i]);
    }
  }
}
