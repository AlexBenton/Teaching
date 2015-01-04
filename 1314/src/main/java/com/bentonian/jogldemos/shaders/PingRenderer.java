package com.bentonian.jogldemos.shaders;

import javax.media.opengl.GL4;

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
    GL4 gl = shaderDemo.getGl();
    M3d ping = shaderDemo.getPingPoint();
    long now = System.currentTimeMillis();

    if (ping == null) {
      ping = ORIGIN;
      t = 0;
    } else {
      t += (now - tick) * 0.0005f;
    }
    tick = now;
    gl.glUniform1f(gl.glGetUniformLocation(shaderProgram, "Time"), t);
    gl.glUniform3f(gl.glGetUniformLocation(shaderProgram, "Ping"), 
        (float) ping.getX(),
        (float) ping.getY(),
        (float) ping.getZ());
    super.render(shaderDemo, model);
  }
}
