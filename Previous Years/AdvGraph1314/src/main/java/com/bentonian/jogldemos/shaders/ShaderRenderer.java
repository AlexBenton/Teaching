package com.bentonian.jogldemos.shaders;

import javax.media.opengl.GL4;

import com.bentonian.framework.io.ShaderUtil;


public class ShaderRenderer {
  
  private final String vertexShader;
  private final String fragmentShader;

  protected int vShader = -1;
  protected int fShader = -1;
  protected int shaderProgram = -1;

  public ShaderRenderer(String vertexShader, String fragmentShader) {
    this.vertexShader = vertexShader;
    this.fragmentShader = fragmentShader;
  }

  protected void init(ShaderDemo shaderDemo) {
    GL4 gl = shaderDemo.getGl();
    if (shaderProgram == -1) {
      vShader = ShaderUtil.loadShader(gl, GL4.GL_VERTEX_SHADER, ShaderRenderer.class, vertexShader);
      fShader = ShaderUtil.loadShader(gl, GL4.GL_FRAGMENT_SHADER, ShaderRenderer.class, fragmentShader);
      shaderProgram = ShaderUtil.compileProgram(gl, vShader, fShader);
    }
    shaderDemo.useProgram(shaderProgram);
  }

  public void disable(ShaderDemo shaderDemo) {
  }

  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    model.getGeometry().render(shaderDemo);
  }
}
