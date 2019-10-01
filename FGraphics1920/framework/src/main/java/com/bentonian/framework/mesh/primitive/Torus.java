package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathUtil.solveQuartic;
import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshVertex;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.texture.TexCoord;

public class Torus extends MeshPrimitiveWithTexture implements IsRayTraceable {

  public static final int di = 75;
  public static final int dj = 25;
  public static final double DEFAULT_MAJOR_RADIUS = 1.6;
  public static final double DEFAULT_MINOR_RADIUS = 0.4;

  private final double R;         // Major radius
  private final double r;         // Minor radius

  public Torus() {
    this(DEFAULT_MAJOR_RADIUS, DEFAULT_MINOR_RADIUS);
  }

  public Torus(double R, double r) {
    this.R = R;
    this.r = r;

    MeshVertex[][] vertices = new MeshVertex[di][dj];
    for (int i = 0; i < di; i++) {
      for (int j = 0; j < dj; j++) {
        vertices[i][j] = new MeshVertex(getTorusVertex(i, j));
      }
    }
    for (int i = 0; i < di; i++) {
      for (int j = 0; j < dj; j++) {
        getMesh().add(new MeshFace(
            vertices[i][j],
            vertices[i][(j + 1) % dj],
            vertices[(i + 1) % di][(j + 1) % dj],
            vertices[(i + 1) % di][j]));
      }
    }
    getMesh().computeAllNormals();
    setRenderStyle(RenderStyle.NORMALS_BY_VERTEX);
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
    int numSolutions = solveQuartic(coefficients, solutions);

    RayIntersections hitList = new RayIntersections();
    for (int i = 0; i<numSolutions; i++) {
      Vec3 pt = ray.at(solutions[i]);
      hitList.add(this, solutions[i], pt, getNormal(pt), getMaterial(pt));
    }

    return hitList;
  }

  @Override
  public TexCoord getTextureCoord(Vec3 pt) {
    double u, v;
    double len = sqrt(pt.getX() * pt.getX() + pt.getZ() * pt.getZ());

    // Determine its angle from the y-axis.
    u = 0.5 - atan2(pt.getZ(), pt.getX()) / (2 * PI);

    // Now rotate about the y-axis to get the point P into the x-z plane.
    v = 0.5 - atan2(pt.getY(), (len - R)) / (2 * PI);

    // Modulo 4 because a torus is too darned long; makes 4x copies of the texture, one per quadrant
    u *= 4;
    u = u - ((int) u);

    return new TexCoord(u, v);
  }

  @Override
  public Vec3 getUBasis(Vec3 pt) {
    double theta = atan2(pt.getZ(), pt.getX());
    return new Vec3(cos(theta + PI / 2), 0, sin(theta + PI / 2));
  }

  private Vec3 getTorusVertex(int i, int j) {
    double u = 2 * PI * i / di;
    double v = 2 * PI * j / dj + PI;
    return new Vec3(
        (R + r * cos(v)) * cos(u),
        r * sin(v),
        (R + r * cos(v)) * sin(u));
  }

  private Vec3 getNormal(Vec3 pt) {
    Vec3 proj = (new Vec3(pt.getX(), 0, pt.getZ())).normalized().times(R);
    return applyTextureToNormal(pt, pt.minus(proj).normalized());
  }
}
