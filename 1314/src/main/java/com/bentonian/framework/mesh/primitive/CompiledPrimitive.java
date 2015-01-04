package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.material.MaterialPrimitive;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.framework.ui.GLVertexData;
import com.google.common.base.Preconditions;

public class CompiledPrimitive extends MaterialPrimitive {

  private final GLVertexData vao;

  protected CompiledPrimitive(GLVertexData.Mode mode) {
    this.vao = new GLVertexData(mode);
  }

  protected CompiledPrimitive(GLVertexData.Mode mode, M3d color) {
    super(color);
    this.vao = new GLVertexData(mode);
  }

  public boolean isCompiled() {
    return vao.isCompiled();
  }

  public void normal(M3d normal) {
    Preconditions.checkState(!vao.isCompiled());
    vao.normal(normal);
  }

  public void color(M3d color) {
    Preconditions.checkState(!vao.isCompiled());
    vao.color(color);
  }

  public void texture(int textureId) {
    Preconditions.checkState(!vao.isCompiled());
    vao.texture(textureId);
  }

  public void textureCoordinates(TexCoord tc) {
    Preconditions.checkState(!vao.isCompiled());
    vao.textureCoordinates(tc);
  }

  public void vertex(M3d point) {
    Preconditions.checkState(!vao.isCompiled());
    vao.vertex(point);
  }
  
  public void dispose() {
    vao.dispose();
  }
  
  @Override
  protected void renderLocal(GLRenderingContext glCanvas) {
    vao.render(glCanvas);
  }
}
