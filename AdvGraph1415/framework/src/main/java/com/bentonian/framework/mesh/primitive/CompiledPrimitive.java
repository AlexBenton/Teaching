package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.material.MaterialPrimitive;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.texture.TexCoord;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;

public class CompiledPrimitive extends MaterialPrimitive {

  private final GLVertexData vao;
  
  public CompiledPrimitive(GLVertexData.Mode mode) {
    this.vao = new GLVertexData(mode);
  }

  public CompiledPrimitive(GLVertexData.Mode mode, M3d color) {
    super(color);
    this.vao = new GLVertexData(mode);
  }

  public boolean isCompiled() {
    return vao.isCompiled();
  }
  
  public void normal(M3d normal) {
    vao.normal(normal);
  }

  public void color(M3d color) {
    vao.color(color);
  }

  public void setHasTexture(boolean hasTexture) {
    if (hasTexture != vao.hasTexture()) {
      vao.setHasTexture(hasTexture);
    }
  }

  public void textureCoordinates(TexCoord tc) {
    vao.textureCoordinates(tc);
  }

  public void vertex(M3d point) {
    vao.vertex(point);
  }
  
  public void dispose() {
    vao.dispose();
  }
  
  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    vao.render(glCanvas);
  }
}
