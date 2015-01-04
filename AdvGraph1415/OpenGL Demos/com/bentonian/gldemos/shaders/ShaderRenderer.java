package com.bentonian.gldemos.shaders;

import static com.bentonian.framework.ui.ShaderUtil.testGlError;

import org.lwjgl.opengl.GL20;

import com.bentonian.framework.ui.ShaderUtil;


public class ShaderRenderer {
  
  private final String vertexShader;
  private final String fragmentShader;

  protected int shaderProgram = -1;

  public ShaderRenderer(String vertexShader, String fragmentShader) {
    this.vertexShader = vertexShader;
    this.fragmentShader = fragmentShader;
  }

  protected void init(ShaderDemo shaderDemo) {
    if (shaderProgram == -1) {
      shaderProgram = ShaderUtil.compileProgram(
          ShaderUtil.loadShader(GL20.GL_VERTEX_SHADER, ShaderRenderer.class, vertexShader), 
          ShaderUtil.loadShader(GL20.GL_FRAGMENT_SHADER, ShaderRenderer.class, fragmentShader));
    }
    shaderDemo.useProgram(shaderProgram);
    testGlError();
  }

  public void disable(ShaderDemo shaderDemo) {
  }

  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    model.getGeometry().render(shaderDemo);
  }
}
