package com.bentonian.framework.mesh.textures;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.GLRenderingContext;

public class Texture {

  public static final M3d BLACK = new M3d(0, 0, 0);
  public static final M3d WHITE = new M3d(1, 1, 1);
  public static final M3d RED = new M3d(203, 65, 84).times(1 / 255.0);
  public static final M3d BLUE = new M3d(84, 65, 203).times(1 / 255.0);
  public static final M3d GREEN = new M3d(65, 203, 84).times(1 / 255.0);
  
  private ImageTexture staticGlRenderCopy;

  public M3d getNormal(IsTextured target, M3d pt, M3d normal) {
    return normal;
  }

  public Material getMaterial(IsTextured target, M3d pt, Material source) {
    Material material = new Material(source);
    material.setColor(getColor(target, pt));
    return material;
  }

  protected M3d getColor(IsTextured target, M3d pt) {
    return WHITE;
  }

  public int getOpenGlTextureId(GLRenderingContext glCanvas) {
    if (staticGlRenderCopy == null) {
      staticGlRenderCopy = new ImageTexture(this, 256, 256);
    }
    return staticGlRenderCopy.getOpenGlTextureId(glCanvas);
  }
}
