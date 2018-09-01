package com.bentonian.framework.texture;

import static com.bentonian.framework.material.Colors.WHITE;

import org.lwjgl.opengl.GL11;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;

public class Texture {

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
