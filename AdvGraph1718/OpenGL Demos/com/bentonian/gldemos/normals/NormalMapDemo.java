package com.bentonian.gldemos.normals;

import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.ShaderUtil;

public class NormalMapDemo extends DemoApp {

  private final Sphere sphere;
  
  protected NormalMapDemo() {
    super("Normals");
    sphere = new Sphere(80, 40);
    sphere.scale(4);
  }
  
  @Override
  public void initGl() {
    super.initGl();
    useProgram(ShaderUtil.compileProgram(
        ShaderUtil.loadShader(GL20.GL_VERTEX_SHADER, NormalMapDemo.class, "normals.vsh"),
        ShaderUtil.loadShader(GL20.GL_FRAGMENT_SHADER, NormalMapDemo.class, "normals.fsh")));
  }

  @Override
  public void draw() {
    sphere.render(this);
  }

  @Override
  protected M3d getLightPosition() {
    long tick = System.currentTimeMillis();
    return new M3d(-10 * Math.cos(tick / 1000.0), 10 * Math.sin(tick / 1000.0), 10);
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new NormalMapDemo().run();
  }
}
