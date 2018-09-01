package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;

public class Plane extends CompiledPrimitive implements IsRayTraceable {

  private static final M3d Y_AXIS = new M3d(0,1,0);
  private static final M3d GREY = new M3d(0.5, 0.5, 0.5);

  public Plane() {
    super(GLVertexData.Mode.QUADS, GREY);
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    double t = -ray.origin.getY() / ray.direction.getY();

    if (t >= MathConstants.EPSILON) {
      return new RayIntersections().add(this, t, ray.at(t), Y_AXIS, getMaterial());
    }
    return null;
  }

  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    if (!isCompiled()) {
      color(getColor());
      normal(Y_AXIS);
      vertex(new M3d(-25,0,-25));
      vertex(new M3d(25,0,-25));
      vertex(new M3d(25,0,25));
      vertex(new M3d(-25,0,25));
    }
    super.renderLocal(glCanvas);
  }
}
