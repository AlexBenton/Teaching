package com.bentonian.framework.mesh.advanced;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.mesh.textures.IsTextured;
import com.bentonian.framework.mesh.textures.Texture;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.bentonian.framework.ui.GLRenderingContext;

public class TexturedSphere extends Sphere implements IsTextured {

  protected Texture texture;

  public TexturedSphere(Texture texture) {
    this.texture = texture;
  }
  
  @Override
  public TexCoord getTextureCoord(M3d pt) {
    double u = 0.5 + atan2(pt.getZ(), pt.getX()) / (2 * PI);
    double v = 0.5 - asin(pt.getY()) / PI;
    return new TexCoord(u, v);
  }

  @Override
  protected void addVertex(M3d vertex) {
    textureCoordinates(getTextureCoord(vertex));
    super.addVertex(vertex);
  }
  
  @Override
  public M3d getUBasis(M3d pt) {
    double theta = atan2(pt.getZ(), pt.getX());
    return new M3d(cos(theta + PI / 2), 0, sin(theta + PI / 2));
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
