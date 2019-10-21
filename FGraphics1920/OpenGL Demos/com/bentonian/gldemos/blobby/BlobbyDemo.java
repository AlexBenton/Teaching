package com.bentonian.gldemos.blobby;

import java.awt.Color;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.Queue;

import org.lwjgl.glfw.GLFW;

import com.bentonian.framework.material.Colors;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.implicits.ForceFunction;
import com.bentonian.framework.mesh.implicits.ImplicitSurfaceMesh;
import com.bentonian.framework.mesh.implicits.MetaBall;
import com.bentonian.framework.mesh.implicits.OctreeEdgeInterpolationData;
import com.bentonian.framework.scene.ControlWidget;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLFWCanvas;
import com.bentonian.framework.ui.GLWindowedAppSecondaryFrame;

public class BlobbyDemo extends DemoApp {

  private final FunctionFrame functionFrame;
  private final ImplicitSurfaceMesh surface;
  private final Mover red, blue;

  private boolean paused = false;
  private boolean moved = false;
  private double t = 0;
  private MoverForceFunction forceFunction;
  private ForceFunction surfaceFunction;
  
  // Timing tracking
  long now = 0;
  long then = 0;
  Queue<Long> times = new LinkedList<Long>();

  public BlobbyDemo() {
    super("Blobby Demo");
    this.forceFunction = MoverForceFunction.WYVILL;
    this.functionFrame = new FunctionFrame(this);
    this.red = new Mover(4, 0, 0, Colors.RED) {
      @Override
      public void update(double t) {
        Vec3 A = blue.getMovedPos();
        Vec3 B = getMovedPos();
        t = 0.5 + 0.5 * Math.cos(t);
        Vec3 interpolated = A.plus(B.minus(A).times(t));
        translate(interpolated.minus(getPosition()));
      }
    };
    this.blue = new Mover(-4, 0, 0, Colors.BLUE);
    this.surfaceFunction = new ForceFunction()
        .addForce(red.getMetaBall())
        .addForce(blue.getMetaBall());
    this.surface = new ImplicitSurfaceMesh(new Vec3(-8,-8,-8), new Vec3(8,8,8), surfaceFunction)
        .setTargetLevel(5)
        .refineCompletely();
    setCameraDistance(15);
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case GLFW.GLFW_KEY_SPACE:
      paused = !paused;
      break;
    case GLFW.GLFW_KEY_B:
      surface.setShowBoxes(!surface.getShowBoxes());
      break;
    case GLFW.GLFW_KEY_C:
      surface.setBlendColors(!surface.getShowColors());
      surface.dispose();
      break;
    case GLFW.GLFW_KEY_E:
      surface.setShowEdges(!surface.getShowEdges());
      break;
    case GLFW.GLFW_KEY_F:
      surface.setShowFaces(!surface.getShowFaces());
      break;
    case GLFW.GLFW_KEY_L:
      if (functionFrame.isVisible()) {
        forceFunction = forceFunction.next();
        functionFrame.repaint();
        surface.reset();
        surface.refineCompletely();
      } else {
        functionFrame.setInnerSize(400, 400);
        functionFrame.setLocation(getLeft() + getWidth() + 30, getTop());
        functionFrame.setVisible(true);
      }
      break;
    case GLFW.GLFW_KEY_S:
      OctreeEdgeInterpolationData.setSmoothEdgeInterpolation(
          !OctreeEdgeInterpolationData.getSmoothEdgeInterpolation());
      surface.reset();
      break;
    case GLFW.GLFW_KEY_EQUAL:
      surface.setTargetLevel(Math.min(10, surface.getTargetLevel() + 1));
      break;
    case GLFW.GLFW_KEY_MINUS:
      surface.setTargetLevel(Math.max(1, surface.getTargetLevel() - 1));
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  @Override
  protected void draw() {
    preTick();
    surface.render(this);
    red.render(this);
    blue.render(this);
    postTick();
  }
  
  @Override
  protected String getScreenshotTitle() {
    return "Blobby Demo";
  }

  private void preTick() {
    now = System.currentTimeMillis();
    if (then == 0) {
      then = now;
    }
    if (!paused) {
      t += ((now-then) * 15.0 / 1000.0) * Math.PI/64.0;
      red.update(t);
      blue.update(t);
    }
    if (moved) {
      surface.reset();
      surface.refineCompletely();
      moved = false;
    }
    then = now;
  }

  private void postTick() {
    long now = System.currentTimeMillis();
    times.add(now);
    while (now - times.peek() > 1000) {
      times.remove();
    }
    float delta = (now - times.peek()) / 1000.0f;
    if (delta > 0) {
      setTitle("Blobby Demo: Level " + surface.getTargetLevel() + ".  " 
          + surface.getNumPolys() + " polys @ "
          + String.format("%.02f", times.size() / delta) + " fps");
    }
  }

  /////////////////////////////////////////////////////////////////////////////
  
  private enum MoverForceFunction {
    WYVILL("Wyvill") {
      @Override
      public double F(double r) {
        return MetaBall.wyvill(r, 1);
      }
    },
    BASIC("1 - r / 4") {
      @Override
      public double F(double r) {
        return 1 - r / 4;
      }
    },
    SHARP("(r < 2) ? 1 : 0") {
      @Override
      public double F(double r) {
        return (r < 2) ? 1 : 0;
      }
    },
    TIGHT("1/2 - atan((r-2) * 5) / PI") {
      @Override
      public double F(double r) {
        return 0.5 - Math.atan((r - 2) * 5) / Math.PI;
      }
    },
    ;
    
    private final String title;
    
    private MoverForceFunction(String title) {
      this.title = title;
    }
    
    public MoverForceFunction next() {
      return values()[(this.ordinal()+1) % values().length];
    }
    
    public String getTitle() {
      return title;
    }
    
    public abstract double F(double r);
  }

  private class Mover extends ControlWidget {
    final MetaBall metaball;
    
    Vec3 movedPos;

    public Mover(double x, double y, double z, Vec3 color) {
      movedPos = new Vec3(x, y, z);
      metaball = new MetaBall(x, y, z, 1.0, color) {
        @Override
        public double F(Vec3 v) {
          return forceFunction.F(this.minus(v).length());
        }
      };
      BlobbyDemo.this.registerMouseHandler(this);
      translate(new Vec3(x, y, z));
    }
    
    public void update(double t) {
    }

    @Override
    public Mover translate(Vec3 delta) {
      super.translate(delta);
      metaball.set(getPosition());
      moved = true;
      return this;
    }

    protected void moveWidget(Vec3 delta) {
      super.moveWidget(delta);
      movedPos = getPosition();
    }
    
    public MetaBall getMetaBall() {
      return metaball;
    }
    
    public Vec3 getMovedPos() {
      return movedPos;
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private class FunctionFrame extends GLWindowedAppSecondaryFrame {

    public FunctionFrame(GLFWCanvas app) {
      super(app, "Force function");
    }

    @Override
    public void paint(Graphics g){
      super.paint(g);
      setTitle(forceFunction.getTitle());
      setAlwaysOnTop(true);
      g.setColor(Color.WHITE);
      g.fillRect(getInnerLeft(), getInnerTop(), getInnerWidth(), getInnerHeight());
      g.setColor(Color.LIGHT_GRAY);
      g.drawRect(getInnerLeft() + 8, getInnerTop() + 8, getInnerWidth() - 16, getInnerHeight() - 16);
      for (int i = 1; i < 4; i++) {
        g.drawLine(
            getInnerLeft() + (int) (10 + (i / 4.0) * (getInnerWidth() - 20)), 
            getInnerTop() + (int) (getInnerHeight() - 10),
            getInnerLeft() + (int) (10 + (i / 4.0) * (getInnerWidth() - 20)),
            getInnerTop() + (int) (getInnerHeight() - 10 - 0.05 * (getInnerHeight() - 20)));
      }
      g.setColor(Color.BLACK);
      double prevForce = forceFunction.F(0);
      for (int i = 1; i < getInnerWidth() - 20; i++) {
        double t = i / (double) (getInnerWidth() - 20);
        double radius = t * 4;
        double force = forceFunction.F(radius);
        g.drawLine(
            getInnerLeft() + 10 + i - 1, 
            getInnerTop() + (int) (getInnerHeight() - 10 - prevForce * (getInnerHeight() - 20)),
            getInnerLeft() + 10 + i,
            getInnerTop() + (int) (getInnerHeight() - 10 - force * (getInnerHeight() - 20)));
        prevForce = force;
      }
      g.setColor(Color.RED);
      g.drawLine(
          getInnerLeft() + 10,
          getInnerTop() + (int) (getInnerHeight() - 10 - surfaceFunction.getCutoff() * (getInnerHeight() - 20)),
          getInnerLeft() + getInnerWidth() - 20,
          getInnerTop() + (int) (getInnerHeight() - 10 - surfaceFunction.getCutoff() * (getInnerHeight() - 20)));
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new BlobbyDemo().run();
  }
}
