package com.bentonian.gldemos.shaders;

import static com.bentonian.framework.ui.ShaderUtil.testGlError;

import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M3d;

public class PingRenderer extends ShaderRenderer {

  private static final M3d ORIGIN = new M3d(0,0,0);

  private float t = 0;
  private long tick;
  
  public PingRenderer() {
    super("ping.vsh", "ping.fsh");
    this.tick = System.currentTimeMillis();
  }

  @Override
  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    M3d ping = shaderDemo.getPingPoint();
    long now = System.currentTimeMillis();

    testGlError();
    if (ping == null) {
      ping = ORIGIN;
      t = 0;
    } else {
      t += (now - tick) * 0.0005f;
    }
    tick = now;
    GL20.glUniform1f(GL20.glGetUniformLocation(shaderProgram, "Time"), t);
    GL20.glUniform3f(GL20.glGetUniformLocation(shaderProgram, "Ping"), 
        (float) ping.getX(),
        (float) ping.getY(),
        (float) ping.getZ());
    super.render(shaderDemo, model);
    testGlError();
  }
}
