package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathConstants.CORNERS_OF_A_SQUARE;
import static com.bentonian.framework.math.MathConstants.EPSILON;
import static java.lang.Math.abs;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.texture.TexCoord;
import com.bentonian.framework.ui.Vertex;
import com.google.common.base.Preconditions;

public class Square extends MeshPrimitiveWithTexture implements IsRayTraceable {

  private static final M3d X_AXIS = new M3d(1, 0, 0);
  private static final M3d Z_AXIS = new M3d(0, 0, 1);
  
  public Square() {
    getMesh().add(new MeshFace(
        new Vertex(CORNERS_OF_A_SQUARE[0]),
        new Vertex(CORNERS_OF_A_SQUARE[1]),
        new Vertex(CORNERS_OF_A_SQUARE[2]),
        new Vertex(CORNERS_OF_A_SQUARE[3])));
    getMesh().computeAllNormals();
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    if (Math.abs(ray.direction.getZ()) > EPSILON) {
      double t = -(ray.origin.getZ()) / ray.direction.getZ();
      if (t > EPSILON) {
        M3d pt = ray.at(t);
        Preconditions.checkState(abs(pt.getZ()) < EPSILON);
        if (abs(pt.getX()) <= 1 + EPSILON && abs(pt.getY()) <= 1 + EPSILON) {
          return new RayIntersections().add(this, t, pt, getNormal(pt), getMaterial(pt));
        }
      }
    }
    return null;
  }

  private M3d getNormal(M3d pt) {
    return applyTextureToNormal(pt, Z_AXIS);
  }

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    return new TexCoord((pt.getX() + 1) / 2.0, (1 - pt.getY()) / 2.0);
  }
  
  @Override
  public M3d getUBasis(M3d pt) {
    return X_AXIS;
  }
}
