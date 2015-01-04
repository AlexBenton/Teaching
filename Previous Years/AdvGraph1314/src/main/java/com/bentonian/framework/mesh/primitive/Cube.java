package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathConstants.FACES_OF_A_CUBE;
import static com.bentonian.framework.math.MathConstants.NORMALS_OF_A_CUBE;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.framework.ui.GLVertexData;

public class Cube extends CompiledPrimitive implements IsRayTraceable {

  public Cube() {
    super(GLVertexData.Mode.QUADS);
  }

  public Cube(M3d color) {
    super(GLVertexData.Mode.QUADS, color);
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    RayIntersections hitList = new RayIntersections();
    collide(hitList, -(ray.origin.getX()-1) / ray.direction.getX(), ray);
    collide(hitList, -(ray.origin.getX()+1) / ray.direction.getX(), ray);
    collide(hitList, -(ray.origin.getY()-1) / ray.direction.getY(), ray);
    collide(hitList, -(ray.origin.getY()+1) / ray.direction.getY(), ray);
    collide(hitList, -(ray.origin.getZ()-1) / ray.direction.getZ(), ray);
    collide(hitList, -(ray.origin.getZ()+1) / ray.direction.getZ(), ray);
    return hitList;
  }

  private boolean collide(RayIntersections hitList, double t, Ray ray) {
    if (t > MathConstants.EPSILON) {
      M3d pt = ray.at(t);
      if (Math.abs(pt.getX()) <= 1.00001 &&
          Math.abs(pt.getY()) <= 1.00001 &&
          Math.abs(pt.getZ()) <= 1.00001) {
        hitList.add(t, pt, getNormal(pt), getMaterial(pt));
      }
    }

    return false;
  }

  protected M3d getNormal(M3d pt) {
    return pt.toAxis();
  }

  protected Material getMaterial(M3d pt) {
    return getMaterial();
  }

  @Override
  protected void renderLocal(GLRenderingContext glCanvas) {
    if (!isCompiled()) {
      color(getColor());
      for (int face = 0; face < 6; face++) {
        addFace(face);
      }
    }
    super.renderLocal(glCanvas);
  }

  protected void addFace(int face) {
    normal(NORMALS_OF_A_CUBE[face]);
    for (int i = 0; i < 4; i++) {
      vertex(FACES_OF_A_CUBE[face][i]);
    }
  }
}
