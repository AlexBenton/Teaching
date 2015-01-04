package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathUtil.SolveQuartic;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.framework.ui.GLVertexData;

public class Torus extends CompiledPrimitive implements IsRayTraceable {

  public static final int di = 75;
  public static final int dj = 25;
  public static final double DEFAULT_MAJOR_RADIUS = 1.6;
  public static final double DEFAULT_MINOR_RADIUS = 0.4;

  protected final double R;         // Major radius
  protected final double r;         // Minor radius

  public Torus() {
    this(DEFAULT_MAJOR_RADIUS, DEFAULT_MINOR_RADIUS);
  }

  public Torus(M3d color) {
    this(color, DEFAULT_MAJOR_RADIUS, DEFAULT_MINOR_RADIUS);
  }

  public Torus(double R, double r) {
    this(new M3d(1, 1, 1), R, r);
  }
  
  public Torus(M3d color, double R, double r) {
    super(GLVertexData.Mode.QUADS, color);
    this.R = R;
    this.r = r;
  }

  /**
   * Notation and maths from Graphics Gems II, p. 252
   */
  @Override
  public RayIntersections traceLocal(Ray ray) {
    double ax = ray.direction.getX();
    double ay = ray.direction.getY();
    double az = ray.direction.getZ();
    double x0 = ray.origin.getX();
    double y0 = ray.origin.getY();
    double z0 = ray.origin.getZ();
    double p = (r*r)/(r*r); // Square of the elliptical ratio x/y
    double A0 = 4*R*R;
    double B0 = (R*R - r*r);
    double C0 = ax*ax + p*ay*ay + az*az;
    double D0 = x0*ax + p*y0*ay + z0*az;
    double E0 = x0*x0 + p*y0*y0 + z0*z0 + B0;
    double[] coefficients = {
        E0*E0 - A0*(x0*x0+z0*z0),
        4*D0*E0 - 2*A0*(x0*ax+z0*az),
        4*D0*D0 + 2*E0*C0 - A0*(ax*ax+az*az),
        4*D0*C0,
        C0*C0
    };
    double[] solutions = new double[4];
    int numSolutions = SolveQuartic(coefficients, solutions);

    RayIntersections hitList = new RayIntersections();
    for (int i = 0; i<numSolutions; i++) {
      M3d pt = ray.at(solutions[i]);
      hitList.add(solutions[i], pt, getNormal(pt), getMaterial(pt));
    }

    return hitList;
  }

  protected Material getMaterial(M3d pt) {
    return getMaterial();
  }

  @Override
  protected void renderLocal(GLRenderingContext glCanvas) {
    if (!isCompiled()) {
      color(getColor());
      for (int i = 0; i < di; i++) {
        for (int j = 0; j < dj; j++) {
          for (int[] step : new int[][] { { 0, 0 }, { 0, 1 }, { 1, 1 }, { 1, 0 } }) {
            addVertex(i + step[0], j + step[1]);
          }
        }
      }
    }
    super.renderLocal(glCanvas);
  }

  protected void addVertex(int u, int v) {
    M3d point = getVertex(u, v);
    normal(getNormal(point));
    vertex(point);
  }
  
  protected M3d getVertex(int i, int j) {
    double u = 2 * PI * i / di;
    double v = 2 * PI * j / dj + PI;
    return new M3d(
        (R + r * cos(v)) * cos(u),
        r * sin(v),
        (R + r * cos(v)) * sin(u));
  }

  protected M3d getNormal(M3d point) {
    M3d proj = (new M3d(point.getX(), 0, point.getZ())).normalized().times(R);
    return point.minus(proj).normalized();
  }
}
