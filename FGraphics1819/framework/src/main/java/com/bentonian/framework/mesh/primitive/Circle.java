package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathConstants.X_AXIS;

import com.bentonian.framework.material.Colors;
import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.texture.TexCoord;
import com.bentonian.framework.ui.GLCanvas;

public class Circle extends MeshPrimitiveWithTexture implements IsRayTraceable {

  private static final M3d Y_AXIS = new M3d(0,1,0);
  private static final Material RING = new Material().setColor(new M3d(0.2, 0.2, 1));
  private static final float RADIUS = 1;

  private final Material black;
  private final Material white;

  public Circle() {
    this.black = new Material().setColor(Colors.BLACK);
    this.white = new Material().setColor(Colors.WHITE);
    setTexture(BufferedImageTexture.CHECKERBOARD);
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    double t = -ray.origin.getY() / ray.direction.getY();

    if (t >= MathConstants.EPSILON) {
      M3d pt = ray.at(t);
      int x = (int)(Math.floor(pt.getX() * 5));
      int z = (int)(Math.floor(pt.getZ() * 5));

      if (pt.length() <= RADIUS) {
        return new RayIntersections().add(this, t, ray.at(t), Y_AXIS,
            (((x + z) & 0x01) == 0) ? black : white);
      } else if (pt.length() <= RADIUS + 0.05) {
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
      for (int i = 0; i < 48; i++) {
        double t1 = ((double) i) * 2 * Math.PI / 48.0;
        double t2 = ((double) (i+1)) * 2 * Math.PI / 48.0;
        textureCoordinates(new TexCoord(0.5, 0.5));
        vertex(new M3d(0, 0, 0));
        textureCoordinates(new TexCoord(Math.cos(t1) / 2 + 0.5, Math.sin(t1) / 2 + 0.5));
        vertex(new M3d(RADIUS * Math.cos(t1), 0, RADIUS * Math.sin(t1)));
        textureCoordinates(new TexCoord(Math.cos(t2) / 2 + 0.5, Math.sin(t2) / 2 + 0.5));
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

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    return new TexCoord((pt.getX() + 1) / 2.0, (1 - pt.getY()) / 2.0);
  }

  @Override
  public M3d getUBasis(M3d pt) {
    return X_AXIS;
  }
}
