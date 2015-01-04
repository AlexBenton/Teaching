package com.bentonian.framework.mesh.advanced;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.mesh.textures.IsTextured;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.bentonian.framework.mesh.textures.Texture;
import com.bentonian.framework.ui.GLRenderingContext;

public class TexturedSquare extends Square implements IsTextured {

  private static final M3d X_AXIS = new M3d(1, 0, 0);
  private static final TexCoord[] TEXTURE_COORDINATES_OF_A_SQUARE = {
    new TexCoord(0, 0), new TexCoord(0, 1), new TexCoord(1, 1), new TexCoord(1, 0)
  };

  protected Texture texture;

  public TexturedSquare(Texture texture) {
    this.texture = texture;
  }

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    return new TexCoord((pt.getX() + 1) / 2.0, (pt.getY() + 1) / 2.0);
  }
  
  @Override
  public M3d getUBasis(M3d pt) {
    return X_AXIS;
  }

  @Override
  protected void addFace() {
    normal(Z_AXIS);
    for (int i = 0; i < 4; i++) {
      textureCoordinates(TEXTURE_COORDINATES_OF_A_SQUARE[i]);
      vertex(MathConstants.CORNERS_OF_A_SQUARE[i]);
    }
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
