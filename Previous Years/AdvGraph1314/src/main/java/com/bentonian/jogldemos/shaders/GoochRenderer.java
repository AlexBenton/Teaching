package com.bentonian.jogldemos.shaders;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;

public class GoochRenderer extends ShaderRenderer {

  public GoochRenderer() {
    super("gooch.vsh", "gooch.fsh");
  }
  
  @Override
  public void init(ShaderDemo shaderDemo) {
    super.init(shaderDemo);
    GL4 gl = shaderDemo.getGl();
    gl.glLineWidth(4);
    gl.glPolygonMode(GL.GL_FRONT, GL4.GL_FILL);
    gl.glPolygonMode(GL.GL_BACK, GL4.GL_LINE);
  }

  @Override
  public void disable(ShaderDemo shaderDemo) {
    GL4 gl = shaderDemo.getGl();
    gl.glPolygonMode(GL.GL_FRONT, GL4.GL_FILL);
    gl.glPolygonMode(GL.GL_BACK, GL4.GL_FILL);
    gl.glLineWidth(1);
    super.disable(shaderDemo);
  }
}
