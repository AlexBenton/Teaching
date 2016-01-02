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
  
  public void setHasTexture(boolean hasTexture) {
    if (hasTexture != vao.hasTexture()) {
      vao.setHasTexture(hasTexture);
    }
  }
  
  public void dispose() {
    vao.dispose();
  }

  protected void normal(M3d normal) {
    vao.normal(normal);
  }

  protected void color(M3d color) {
    vao.color(color);
  }

  protected void textureCoordinates(TexCoord tc) {
    vao.textureCoordinates(tc);
  }

  protected void vertex(M3d point) {
    vao.vertex(point);
  }
  
  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    vao.render(glCanvas);
  }
}
