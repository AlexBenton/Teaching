package com.bentonian.gldemos.shaders;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;

public class GoochRenderer extends ShaderRenderer {

  public GoochRenderer() {
    super("gooch.vsh", "gooch.fsh");
  }

  @Override
  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    
    shaderDemo.getProjection().pushReversed(M4x4.translationMatrix(new M3d(0, 0, -0.0001)));
    GL11.glCullFace(GL11.GL_FRONT);
    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
    GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "bFront"), 0);
    super.render(shaderDemo, model);

    shaderDemo.getProjection().pop();
    GL11.glCullFace(GL11.GL_BACK);
    GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
    GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgram, "bFront"), 1);
    super.render(shaderDemo, model);
  }
}
