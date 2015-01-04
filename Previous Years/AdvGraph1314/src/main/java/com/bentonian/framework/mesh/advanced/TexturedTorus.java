package com.bentonian.framework.mesh.advanced;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Torus;
import com.bentonian.framework.mesh.textures.IsTextured;
import com.bentonian.framework.mesh.textures.Texture;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.bentonian.framework.ui.GLRenderingContext;

public class TexturedTorus extends Torus implements IsTextured {

  protected Texture texture;

  public TexturedTorus(Texture texture) {
    this(texture, DEFAULT_MAJOR_RADIUS, DEFAULT_MINOR_RADIUS);
  }

  public TexturedTorus(Texture texture, final double R, final double r) {
    super(R, r);
    this.texture = texture;
  }

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    double u, v;
    double len = sqrt(pt.getX() * pt.getX() + pt.getZ() * pt.getZ());

    // Determine its angle from the y-axis.
    u = 0.5 + atan2(pt.getZ(), pt.getX()) / (2 * PI);

    // Now rotate about the y-axis to get the point P into the x-z plane.
    v = 0.5 + atan2(pt.getY(), (len - R)) / (2 * PI);

    u *= 4;
    u = u - ((int) u);
    
    return new TexCoord(u, v);
  }
  
  @Override
  public M3d getUBasis(M3d pt) {
    double theta = atan2(pt.getZ(), pt.getX());
    return new M3d(cos(theta + PI / 2), 0, sin(theta + PI / 2));
  }

  @Override
  protected void addVertex(int u, int v) {
    textureCoordinates(getTextureCoord(getVertex(u, v)));
    super.addVertex(u, v);
  }
  

  @Override
  protected void renderLocal(GLRenderingContext glCanvas) {
    if (!isCompiled()) {
      texture(texture.getOpenGlTextureId(glCanvas));
    }
    super.renderLocal(glCanvas);
  }
  
  @Override
  protected M3d getNormal(M3d pt) {
    return texture.getNormal(this, pt, super.getNormal(pt));
  }

  @Override
  protected Material getMaterial(M3d pt) {
    return texture.getMaterial(this, pt, super.getMaterial(pt));
  }
}
