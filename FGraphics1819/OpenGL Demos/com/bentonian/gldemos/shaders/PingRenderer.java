package com.bentonian.gldemos.shaders;

import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M3d;

public class PingRenderer extends ShaderRenderer {

  private static final M3d ORIGIN = new M3d(0,0,0);

  public PingRenderer() {
    super("ping.vsh", "ping.fsh");
  }

  @Override
  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    M3d ping = shaderDemo.getPingPoint();

    if (ping == null) {
      ping = ORIGIN;
      t = 0;
    }
    GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "Ping"), 
        (float) ping.getX(),
        (float) ping.getY(),
        (float) ping.getZ());
    super.render(shaderDemo, model);
  }
}
