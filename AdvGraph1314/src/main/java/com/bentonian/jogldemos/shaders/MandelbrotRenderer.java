package com.bentonian.jogldemos.shaders;

import javax.media.opengl.GL4;

public class MandelbrotRenderer extends ShaderRenderer {

  public MandelbrotRenderer() {
    super("mandelbrot.vsh", "mandelbrot.fsh");
  }

  @Override
  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    float d = (float) shaderDemo.getCameraDistance() - 1;
    float numSteps = Math.max(5, 100 - d * 90 / 4) * (float) Math.pow(shaderDemo.getMandelbrotZoom(), 0.333);
    GL4 gl = shaderDemo.getGl();

    gl.glUniform1f(gl.glGetUniformLocation(shaderProgram, "NumSteps"), numSteps);
    gl.glUniform1f(gl.glGetUniformLocation(shaderProgram, "Zoom"), shaderDemo.getMandelbrotZoom());
    gl.glUniform2f(gl.glGetUniformLocation(shaderProgram, "Center"), 
        shaderDemo.getMandelbrotCenterX(),
        shaderDemo.getMandelbrotCenterY());        
    super.render(shaderDemo, model);
  }
}
