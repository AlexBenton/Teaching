package com.bentonian.gldemos.particles;

import static com.bentonian.framework.ui.ShaderUtil.compileProgram;
import static com.bentonian.framework.ui.ShaderUtil.loadShader;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.PointPrimitive;
import com.bentonian.framework.scene.ControlWidget;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.Vertex;

public class ParticlesDemo extends DemoApp {

  private static final int NUM_PARTICLES = 1000;
  
  private final Vertex particles[] = new Vertex[NUM_PARTICLES];
  private final Vec3 dirs[] = new Vec3[NUM_PARTICLES];
  private final Random rand = new Random();
  private final PointPrimitive pointset;
  private final Cube floor;
  private final ControlWidget ball;
  
  public ParticlesDemo() {
    super("Particles Demo");
    this.pointset = new PointPrimitive();
    this.floor = new Cube();
    this.ball = new ControlWidget();

    pointset.normal(new Vec3(1).normalized());
    floor.scale(new Vec3(2, 0.05, 2));
    floor.translate(new Vec3(0, -0.05, 0));
    ball.scale(0.1);
    for (int i = 0; i < NUM_PARTICLES; i++) {
      dirs[i] = new Vec3(0.5 + 0.5 * rand.nextDouble(), 0.5 + 0.5 * rand.nextDouble(), 0.5 + 0.5 * rand.nextDouble()).normalized();
      particles[i] = pointset.vertex(new Vec3(0));
    }
    setCameraDistance(4);
  }
  
  @Override
  protected void initGl() {
    super.initGl();
    
    GL11.glPointSize(5);
    int vsName = loadShader(GL20.GL_VERTEX_SHADER, GLCanvas.class, "default.vsh");
    int fsName = loadShader(GL20.GL_FRAGMENT_SHADER, ParticlesDemo.class, "particles.fsh");
    int program = compileProgram(vsName, fsName);
    useProgram(program);
  }

  @Override
  public void preDraw() {
    long now = System.currentTimeMillis();
    double dt = (now - lastFrameStartMillis) / 1000.0;

//    dt /= 10.0;
    pointset.dispose();
    for (int i = 0; i < NUM_PARTICLES; i++) {
      Vec3 v = particles[i];
      Vec3 d = dirs[i];
      d.set(
          d.getX(), 
          d.getY() - dt * 9.8, 
          d.getZ());
      v.set(
          v.getX() + dt * d.getX(), 
          v.getY() + dt * d.getY(), 
          v.getZ() + dt * d.getZ());
      if (v.getY() < 0) {
        if (d.length() > 0.1) {
          d.set(new Vec3(d.getX(), Math.abs(d.getY()), d.getZ()).times(0.5));
        } else {
          d.set(dir());
          v.set(0);
        }
      }
    }

    super.preDraw();
  }
  
  private Vec3 dir() {
    return new Vec3(
        0.3 - 0.6 * rand.nextFloat(), 
        1, 
        0.3 - 0.6 * rand.nextFloat()).normalized().times(rand.nextFloat() * 2 + 3);
  }

  @Override
  public void draw() {
    pointset.render(this);
    floor.render(this);
    ball.render(this);
  }
  
  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new ParticlesDemo().run();
  }
}
