package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;

public class Circle extends CompiledPrimitive implements IsRayTraceable {

  private static final M3d Y_AXIS = new M3d(0,1,0);
  private static final Material RING = new Material().setColor(new M3d(0.4, 0.4, 1));
  private static final float RADIUS = 5;
  
  private final Material black;
  private final Material white;

  public Circle() {
    super(GLVertexData.Mode.TRIANGLES);
    this.black = new Material().setColor(new M3d(0, 0, 0));
    this.white = new Material().setColor(new M3d(1, 1, 1));
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    double t = -ray.origin.getY() / ray.direction.getY();
    
    if (t >= MathConstants.EPSILON) {
      M3d pt = ray.at(t);
      int x = (int)(Math.floor(pt.getX()));
      int z = (int)(Math.floor(pt.getZ()));
      
      if (pt.length() <= RADIUS) {
        return new RayIntersections().add(this, t, ray.at(t), Y_AXIS, 
            (((x + z) & 0x01) == 0) ? black : white);
      } else if (pt.length() <= RADIUS + 0.25) {
        return new RayIntersections().add(this, t, ray.at(t), Y_AXIS, RING);
      }
    }
    return null;
  }

  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    if (!isCompiled()) {
      normal(Y_AXIS);
      color(RING.getColor());
      for (int i = 0; i < 12; i++) {
        double t1 = ((double) i) * 2 * Math.PI / 12.0;
        double t2 = ((double) (i+1)) * 2 * Math.PI / 12.0;
        vertex(new M3d(0, 0, 0));
        vertex(new M3d(RADIUS * Math.cos(t1), 0, RADIUS * Math.sin(t1)));
        vertex(new M3d(RADIUS * Math.cos(t2), 0, RADIUS * Math.sin(t2)));
      }
    }
    super.renderLocal(glCanvas);
  }

  @Override
  public Circle setReflectivity(double reflectivity) {
    super.setReflectivity(reflectivity);
    this.black.setReflectivity(reflectivity);
    this.white.setReflectivity(reflectivity);
    return this;
  }
}
