package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.material.MaterialPrimitive;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.texture.TexCoord;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;
import com.bentonian.framework.ui.Vertex;

public class CompiledPrimitive extends MaterialPrimitive {

  private final GLVertexData vao;
  
  public CompiledPrimitive(GLVertexData vao) {
    this.vao = vao;
  }

  public CompiledPrimitive(GLVertexData.Mode mode) {
    this.vao = new GLVertexData(mode);
  }

  public CompiledPrimitive(GLVertexData.Mode mode, Vec3 color) {
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
  
  public GLVertexData getVertexData() {
    return vao;
  }
  
  public void dispose() {
    vao.dispose();
  }

  public void normal(Vec3 normal) {
    vao.normal(normal);
  }

  public void color(Vec3 color) {
    vao.color(color);
  }

  public void textureCoordinates(TexCoord tc) {
    vao.textureCoordinates(tc);
  }

  public Vertex vertex(Vec3 point) {
    return vao.vertex(point);
  }
  
  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    vao.render(glCanvas);
  }
}
