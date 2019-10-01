package com.bentonian.framework.texture;

import static com.bentonian.framework.material.Colors.WHITE;

import org.lwjgl.opengl.GL11;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.Vec3;

public class Texture {

  protected int textureId;

  public Vec3 getNormal(IsTextured target, Vec3 pt, Vec3 normal) {
    return normal;
  }

  public Material getMaterial(IsTextured target, Vec3 pt, Material source) {
    Material material = new Material(source);
    material.setColor(getColor(target, pt));
    material.setTransparency(getTransparency(target, pt));
    return material;
  }

  public Vec3 getColor(IsTextured target, Vec3 pt) {
    return WHITE;
  }
  
  public double getTransparency(IsTextured target, Vec3 pt) {
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
