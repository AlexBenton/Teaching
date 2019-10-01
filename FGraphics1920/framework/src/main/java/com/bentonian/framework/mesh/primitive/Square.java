package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathConstants.CORNERS_OF_A_SQUARE;
import static com.bentonian.framework.math.MathConstants.EPSILON;
import static java.lang.Math.abs;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshVertex;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.texture.TexCoord;
import com.google.common.base.Preconditions;

public class Square extends MeshPrimitiveWithTexture implements IsRayTraceable {

  private static final Vec3 X_AXIS = new Vec3(1, 0, 0);
  private static final Vec3 Z_AXIS = new Vec3(0, 0, 1);
  
  public Square() {
    getMesh().add(new MeshFace(
        new MeshVertex(CORNERS_OF_A_SQUARE[0]),
        new MeshVertex(CORNERS_OF_A_SQUARE[1]),
        new MeshVertex(CORNERS_OF_A_SQUARE[2]),
        new MeshVertex(CORNERS_OF_A_SQUARE[3])));
    getMesh().computeAllNormals();
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    if (Math.abs(ray.direction.getZ()) > EPSILON) {
      double t = -(ray.origin.getZ()) / ray.direction.getZ();
      if (t > EPSILON) {
        Vec3 pt = ray.at(t);
        Preconditions.checkState(abs(pt.getZ()) < EPSILON);
        if (abs(pt.getX()) <= 1 + EPSILON && abs(pt.getY()) <= 1 + EPSILON) {
          return new RayIntersections().add(this, t, pt, getNormal(pt), getMaterial(pt));
        }
      }
    }
    return null;
  }

  private Vec3 getNormal(Vec3 pt) {
    return applyTextureToNormal(pt, Z_AXIS);
  }

  @Override
  public TexCoord getTextureCoord(Vec3 pt) {
    return new TexCoord((pt.getX() + 1) / 2.0, (1 - pt.getY()) / 2.0);
  }
  
  @Override
  public Vec3 getUBasis(Vec3 pt) {
    return X_AXIS;
  }
}
