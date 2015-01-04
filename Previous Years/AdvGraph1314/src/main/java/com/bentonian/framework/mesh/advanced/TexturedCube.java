package com.bentonian.framework.mesh.advanced;

import static com.bentonian.framework.math.MathConstants.FACES_OF_A_CUBE;
import static com.bentonian.framework.math.MathConstants.NORMALS_OF_A_CUBE;
import static java.lang.Math.abs;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.textures.IsTextured;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.bentonian.framework.mesh.textures.Texture;
import com.bentonian.framework.ui.GLRenderingContext;

public class TexturedCube extends Cube implements IsTextured {

  private static final M3d X_AXIS = new M3d(1, 0, 0);
  private static final M3d Z_AXIS = new M3d(0, 0, 1);
  private static final TexCoord[] TEXTURE_COORDINATES_OF_A_CUBE = {
    new TexCoord(0, 0), new TexCoord(0, 1), new TexCoord(1, 1), new TexCoord(1, 0)
  };

  protected Texture texture;

  public TexturedCube(Texture texture) {
    this.texture = texture;
  }

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    double u, v;
    
    if (abs(abs(pt.getX()) - 1) < MathConstants.EPSILON) {
      u = (pt.getZ() + 1) / 2.0;
      v = (pt.getY() + 1) / 2.0;
    } else if (abs(abs(pt.getY()) - 1) < MathConstants.EPSILON) {
      u = (pt.getX() + 1) / 2.0;
      v = (pt.getZ() + 1) / 2.0;
    } else {
      u = (pt.getX() + 1) / 2.0;
      v = (pt.getY() + 1) / 2.0;
    }
    
    return new TexCoord(u, v);
  }
  
  @Override
  public M3d getUBasis(M3d pt) {
    if (abs(abs(pt.getX()) - 1) < MathConstants.EPSILON) {
      return Z_AXIS;
    } else {
      return X_AXIS;
    }
  }

  @Override
  protected void addFace(int face) {
    normal(NORMALS_OF_A_CUBE[face]);
    for (int i = 0; i < 4; i++) {
      textureCoordinates(TEXTURE_COORDINATES_OF_A_CUBE[i]);
      vertex(FACES_OF_A_CUBE[face][i]);
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
