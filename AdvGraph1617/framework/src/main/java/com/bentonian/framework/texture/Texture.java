package com.bentonian.framework.texture;

import org.lwjgl.opengl.GL11;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;

public class Texture {

  public static final M3d BLACK = new M3d(0, 0, 0);
  public static final M3d GRAY = new M3d(0.5, 0.5, 0.5);
  public static final M3d WHITE = new M3d(1, 1, 1);
  public static final M3d RED = new M3d(203, 65, 84).times(1 / 255.0);
  public static final M3d BLUE = new M3d(84, 65, 203).times(1 / 255.0);
  public static final M3d GREEN = new M3d(65, 203, 84).times(1 / 255.0);
  public static final M3d ORANGE = new M3d(0xFF, 0xA5, 0x00).times(1 / 255.0);
  
  protected int textureId;

  public M3d getNormal(IsTextured target, M3d pt, M3d normal) {
    return normal;
  }

  public Material getMaterial(IsTextured target, M3d pt, Material source) {
    Material material = new Material(source);
    material.setColor(getColor(target, pt));
    material.setTransparency(getTransparency(target, pt));
    return material;
  }

  public M3d getColor(IsTextured target, M3d pt) {
    return WHITE;
  }
  
  public double getTransparency(IsTextured target, M3d pt) {
    return 0.0;
  }

  public void bind() {
    if (textureId == 0) {
      textureId = GL11.glGenTextures();
    }
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
  }

  public void unbind() {
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
  }

  public void dispose() {
    if (textureId != 0) {
      GL11.glDeleteTextures(textureId);
      textureId = 0;
    }
  }
}
