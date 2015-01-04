package com.bentonian.framework.mesh.primitive;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.framework.ui.GLVertexData;

public class Sphere extends CompiledPrimitive implements IsRayTraceable {

  private final int du, dv;

  public Sphere() {
    this(20, 10);
  }

  public Sphere(M3d color) {
    this(color, 20, 10);
  }

  public Sphere(int du, int dv) {
    this(new M3d(1, 1, 1), du, dv);
  }

  public Sphere(M3d color, int du, int dv) {
    super(GLVertexData.Mode.QUADS, color);
    this.du = du;
    this.dv = dv;
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
    hitList.add(t, pt, getNormal(pt), getMaterial(pt));
  }

  protected M3d getNormal(M3d pt) {
    return pt.normalized();
  }

  protected Material getMaterial(M3d pt) {
    return getMaterial();
  }

  @Override
  protected void renderLocal(GLRenderingContext glCanvas) {
    if (!isCompiled()) {
      color(getColor());
      for (int u = 0; u < du; u++) {
        for (int v = 0; v < dv; v++) {
          for (int[] step : new int[][] { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } }) {
            double s = (u + step[0]) * Math.PI * 2 / du;
            double t = (v + step[1]) * Math.PI / dv;
            addVertex(new M3d(cos(s) * sin(t), cos(t), sin(s) * sin(t)));
          }
        }
      }
    }
    super.renderLocal(glCanvas);
  }

  protected void addVertex(M3d vertex) {
    normal(vertex);
    vertex(vertex);
  }
}
