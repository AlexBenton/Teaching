package com.bentonian.gldemos.shaders;

import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.Vec3;

public class PingRenderer extends ShaderRenderer {

  private static final Vec3 ORIGIN = new Vec3(0,0,0);

  public PingRenderer() {
    super("ping.vsh", "ping.fsh");
  }

  @Override
  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    Vec3 ping = shaderDemo.getPingPoint();

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
