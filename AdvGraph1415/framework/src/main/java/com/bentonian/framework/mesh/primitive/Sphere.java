package com.bentonian.framework.mesh.primitive;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
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

public class Sphere extends MeshPrimitiveWithTexture implements IsRayTraceable {

  public Sphere() {
    this(20, 10);
  }

  public Sphere(int du, int dv) {
    this(new M3d(1, 1, 1), du, dv);
  }

  public Sphere(M3d color, int du, int dv) {
    Vertex[][] vertices = new Vertex[du][dv];
    for (int u = 0; u < du; u++) {
      for (int v = 0; v < dv; v++) {
        double s = u * PI * 2 / du;
        double t = v * PI / dv;
        vertices[u][v] = new Vertex(cos(s) * sin(t), -cos(t), sin(s) * sin(t));
      }
    }
    Vertex bottom = new Vertex(0, -1, 0);
    Vertex top = new Vertex(0, 1, 0);
    for (int u = 0; u < du; u++) {
      getMesh().add(new Face(
          bottom,
          vertices[u][1],
          vertices[(u + 1) % du][1]));
      for (int v = 1; v < dv - 1; v++) {
        getMesh().add(new Face(
            vertices[u][v],
            vertices[u][(v + 1) % dv],
            vertices[(u + 1) % du][(v + 1) % dv],
            vertices[(u + 1) % du][v]));
      }
      getMesh().add(new Face(
          vertices[u][dv - 1],
          top,
          vertices[(u + 1) % du][dv - 1]));
    }
    getMesh().computeAllNormals();
    setRenderStyle(RenderStyle.NORMALS_BY_VERTEX);
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    double OdotD = ray.origin.dot(ray.direction);
    double DdotD = ray.direction.dot(ray.direction);
    double OdotO = ray.origin.dot(ray.origin);
    double base = OdotD*OdotD - DdotD*(OdotO-1);

    if (base >= 0) {
      double bm4ac = Math.sqrt(base);
      double t1 = (-OdotD + bm4ac) / DdotD;
      double t2 = (-OdotD - bm4ac) / DdotD;
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
    hitList.add(this, t, pt, getNormal(pt), getMaterial(pt));
  }

  private M3d getNormal(M3d pt) {
    return applyTextureToNormal(pt, pt.normalized());
  }

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    double u = 0.5 + atan2(pt.getZ(), -pt.getX()) / (2 * PI);
    double v = 0.5 - asin(pt.getY()) / PI;
    return new TexCoord(u, v);
  }

  @Override
  public M3d getUBasis(M3d pt) {
    double theta = atan2(pt.getZ(), pt.getX());
    return new M3d(cos(theta + PI / 2), 0, sin(theta + PI / 2));
  }
}
