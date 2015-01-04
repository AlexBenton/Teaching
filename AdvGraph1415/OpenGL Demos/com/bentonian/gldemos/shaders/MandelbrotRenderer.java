package com.bentonian.gldemos.shaders;

import static com.bentonian.framework.ui.ShaderUtil.testGlError;

import org.lwjgl.opengl.GL20;

public class MandelbrotRenderer extends ShaderRenderer {

  public MandelbrotRenderer() {
    super("mandelbrot.vsh", "mandelbrot.fsh");
  }

  @Override
  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    testGlError();
    float d = (float) shaderDemo.getCameraDistance() - 1;
    float numSteps = Math.max(5, 100 - d * 90 / 4) * (float) Math.pow(shaderDemo.getMandelbrotZoom(), 0.333);

    GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "NumSteps"), numSteps);
    GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "Zoom"), shaderDemo.getMandelbrotZoom());
    GL20.glUniform2f(GL20.glGetUniformLocation(shaderProgram, "Center"), 
        shaderDemo.getMandelbrotCenterX(),
        shaderDemo.getMandelbrotCenterY());        
    super.render(shaderDemo, model);
    testGlError();
  }
}
