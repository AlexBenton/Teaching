package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathUtil.isZero;
import static com.bentonian.framework.math.MathUtil.sqr;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.texture.TexCoord;

public class Cylinder extends MeshPrimitiveWithTexture implements IsRayTraceable {

  public Cylinder() {
    this(20, 10);
  }

  public Cylinder(int du, int dv) {
    Vertex[][] vertices = new Vertex[du][dv];
    for (int u = 0; u < du; u++) {
      for (int v = 0; v < dv; v++) {
        double s = u * PI * 2 / du;
        double t = v / (double) dv;
        vertices[u][v] = new Vertex(cos(s), t * 2 - 1, sin(s));
      }
    }
    for (int u = 0; u < du; u++) {
      for (int v = 0; v < dv - 1; v++) {
        getMesh().add(new Face(
            vertices[u][v],
            vertices[u][(v + 1) % dv],
            vertices[(u + 1) % du][(v + 1) % dv],
            vertices[(u + 1) % du][v]));
      }
    }
    getMesh().computeAllNormals();
    setRenderStyle(RenderStyle.NORMALS_BY_VERTEX);
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    double a = sqr(ray.direction.getX()) + sqr(ray.direction.getZ());
    double b = 2 * ray.origin.getX() * ray.direction.getX() + 2 * ray.origin.getZ() * ray.direction.getZ();
    double c = sqr(ray.origin.getX()) + sqr(ray.origin.getZ()) - 1;
    double base = 2 * a;

    if (!isZero(base)) {
      double b2m4ac = Math.sqrt(b * b - 4 * a * c);
      double t1 = (-b + b2m4ac) / base;
      double t2 = (-b - b2m4ac) / base;
      RayIntersections hitList = new RayIntersections();
      hit(hitList, ray, t1);
      hit(hitList, ray, t2);
      return hitList;
    } else {
      return null;
    }
  }

  private void hit(RayIntersections hitList, Ray ray, double t) {
    M3d pt = ray.at(t);
    if (t > 0 && Math.abs(pt.getY()) <= 1) {
      hitList.add(this, t, pt, getNormal(pt), getMaterial(pt));
    }
  }

  private M3d getNormal(M3d pt) {
    return applyTextureToNormal(pt, pt.normalized());
  }

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    double u = 0.5 + atan2(pt.getZ(), -pt.getX()) / (2 * PI);
    double v = (1 - pt.getY()) / 2;
    return new TexCoord(u, v);
  }
  
  @Override
  public M3d getUBasis(M3d pt) {
    double theta = atan2(pt.getZ(), pt.getX());
    return new M3d(cos(theta + PI / 2), 0, sin(theta + PI / 2));
  }
}
