package com.bentonian.gldemos.shaders;

import org.lwjgl.opengl.GL20;

import com.bentonian.framework.ui.ShaderUtil;


public class ShaderRenderer {

  private final String vertexShader;
  private final String fragmentShader;

  protected int shaderProgram = -1;
  protected float t = 0;
  protected long tick;
  
  public ShaderRenderer(String vertexShader, String fragmentShader) {
    this.vertexShader = vertexShader;
    this.fragmentShader = fragmentShader;
    this.tick = System.currentTimeMillis();
  }

  protected void init(ShaderDemo shaderDemo) {
    if (shaderProgram == -1) {
      shaderProgram = ShaderUtil.compileProgram(
          ShaderUtil.loadShader(GL20.GL_VERTEX_SHADER, ShaderRenderer.class, vertexShader),
          ShaderUtil.loadShader(GL20.GL_FRAGMENT_SHADER, ShaderRenderer.class, fragmentShader));
    }
    shaderDemo.useProgram(shaderProgram);
  }

  public void disable(ShaderDemo shaderDemo) {
  }

  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    int timeUniform = GL20.glGetUniformLocation(shaderProgram, "Time");
    if (timeUniform != -1) {
      long now = System.currentTimeMillis();

      t += (now - tick);
      tick = now;
      GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "Time"), t);
    }
    model.getGeometry().render(shaderDemo);
  }

  public String getName() {
    return vertexShader.substring(0, vertexShader.indexOf("."));
  }
}
