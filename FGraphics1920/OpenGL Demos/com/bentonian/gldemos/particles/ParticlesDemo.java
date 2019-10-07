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
import com.bentonian.framework.ui.ShaderAutoloader;
import com.bentonian.framework.ui.Vertex;

public class ParticlesDemo extends DemoApp {

  private static final int NUM_PARTICLES = 10000;
  private static final float BALL_RADIUS = 0.5f;
  
  private final ShaderAutoloader loader;
  private final Vertex particles[] = new Vertex[NUM_PARTICLES];
  private final Vec3 velocities[] = new Vec3[NUM_PARTICLES];
  private final Random rand = new Random();
  private final PointPrimitive pointset;
  private final Cube floor;
  private final ControlWidget ball;
  
  public ParticlesDemo() {
    super("Particles Demo");

    String root = ParticlesDemo.class.getPackage().getName().replace(".", "/") + "/";
    this.loader = new ShaderAutoloader(
        new String[] { root },
        () -> loadShader(GL20.GL_VERTEX_SHADER, root + "default.vsh"),
        () -> loadShader(GL20.GL_FRAGMENT_SHADER, root + "particles.fsh"),
        () -> exitRequested,
        (p) -> useProgram(p),
        (e) -> System.err.println(e)
    );
    
    this.pointset = new PointPrimitive();
    pointset.color(new Vec3(0,1,0.8));

    this.floor = new Cube();
    floor.scale(new Vec3(2, 0.05, 2));
    floor.translate(new Vec3(0, -0.05, 0));

    this.ball = new ControlWidget();
    ball.scale(BALL_RADIUS);
    ball.translate(new Vec3(1, 0.5, 0));
    registerMouseHandler(ball);
    
    for (int i = 0; i < NUM_PARTICLES; i++) {
      velocities[i] = newdir();
      particles[i] = pointset.vertex(new Vec3(0));
      particles[i].setNormal(new Vec3(1));
    }
    setCameraDistance(4);
  }
  
  @Override
  protected void initGl() {
    super.initGl();
    
    GL11.glPointSize(6);
    int vsName = loadShader(GL20.GL_VERTEX_SHADER, GLCanvas.class, "default.vsh");
    int fsName = loadShader(GL20.GL_FRAGMENT_SHADER, ParticlesDemo.class, "particles.fsh");
    int program = compileProgram(vsName, fsName);
    useProgram(program);
  }

  @Override
  public void preDraw() {
    loader.preDraw();

    long now = System.currentTimeMillis();
    double dt = (now - lastFrameStartMillis) / 1000.0;
    Vec3 ballCenter = ball.getPosition();
    Vec3 op = new Vec3();

    pointset.dispose();
    for (int i = 0; i < NUM_PARTICLES; i++) {
      Vertex p = particles[i];
      Vec3 v = velocities[i];
      Vec3 n = p.getNormal();
      op.set(p);

      fall(v, dt);
      move(p, v, dt);
      if (p.getY() < 0) {
        if (op.getY() > 0) {
          double t = op.getY() / (op.getY() - p.getY());
          p.set(op.getX() + t * v.getX(), 0, op.getZ() + t * v.getZ());
          v.set(new Vec3(v.getX(), Math.abs(v.getY()), v.getZ()).times(0.5));
        } else {
          reset(p, v);
        }
      }
      if (ballCenter.minus(p).length() < BALL_RADIUS) {
        if (ballCenter.minus(op).length() > BALL_RADIUS) {
          Vec3 dirNorm = op.minus(ballCenter).normalized();
          Vec3 vNorm = v.normalized();
          Vec3 nVDotN = dirNorm.times(dirNorm.dot(vNorm));
          Vec3 reflection = vNorm.minus(nVDotN.times(2)).normalized();
          v.set(reflection.times(v.length()));
        }
      }

      n.setX(n.getX() - 0.02);
      if (n.getX() < 0) {
        reset(p, v);
      }
    }

    super.preDraw();
  }
  
  private void reset(Vertex p, Vec3 v) {
    v.set(newdir());
    p.set(0);
    p.getNormal().set(0.5 + rand.nextFloat());
  }
  
  private void fall(Vec3 v, double dt) {
    v.set(
        v.getX(), 
        v.getY() - dt * 9.8, 
        v.getZ());
  }
  
  private void move(Vec3 p, Vec3 v, double dt) {
    p.set(
        p.getX() + dt * v.getX(), 
        p.getY() + dt * v.getY(), 
        p.getZ() + dt * v.getZ());    
  }
  
  private Vec3 newdir() {
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
