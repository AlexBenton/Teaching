package com.bentonian.gldemos.blobby;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.lwjgl.glfw.GLFW;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.implicits.ImplicitSurface;
import com.bentonian.framework.mesh.implicits.MetaBall;
import com.bentonian.framework.scene.ControlWidget;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLWindowedApp;
import com.bentonian.framework.ui.GLWindowedAppSecondaryFrame;

public class BlobbyDemo extends DemoApp {

  private static final M3d RED = new M3d(1,0,0);
  private static final M3d BLUE = new M3d(0,0,1);

  private final FunctionFrame functionFrame;
  private final ImplicitSurface surface;
  private final List<Mover> movers;

  private boolean paused = false;
  private boolean moved = false;
  private double t = 0;
  private MoverForceFunction forceFunction;
  
  // Timing tracking
  long now = 0;
  long then = 0;
  Queue<Long> times = new LinkedList<Long>();

  public BlobbyDemo() {
    super("Blobby Demo");
    this.movers = new ArrayList<>();
    this.forceFunction = MoverForceFunction.WYVILL;
    this.functionFrame = new FunctionFrame(this);
    this.surface = new ImplicitSurface(new M3d(-8,-8,-8), new M3d(8,8,8))
        .setTargetLevel(5)
        .addForce(new Mover(
            6.7841952392351645, 2.7370054894176192, -1.7763568394002505E-15,
            RED) {
          @Override
          public void update(double t) {
            translate(new M3d(4 * Math.cos(t), 0, 0).minus(getPosition()));
          }
        }.getMetaBall())
        .addForce(new Mover(-4, 0, 0, BLUE).getMetaBall())
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
    case GLFW.GLFW_KEY_EQUAL:
      surface.setTargetLevel(Math.min(10, surface.getTargetLevel() + 1));
      surface.dispose();
      break;
    case GLFW.GLFW_KEY_MINUS:
      surface.setTargetLevel(Math.max(1, surface.getTargetLevel() - 1));
      surface.dispose();
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  @Override
  protected void draw() {
    preTick();
    surface.render(this);
    for (Mover mover : movers) {
      mover.render(this);
    }
    postTick();
  }
  
  private void preTick() {
    now = System.currentTimeMillis();
    if (then == 0) {
      then = now;
    }
    if (!paused) {
      t += ((now-then) * 15.0 / 1000.0) * Math.PI/64.0;
      for (Mover mover : movers) {
        mover.update(t);
      }
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

    public Mover(double x, double y, double z, M3d color) {
      metaball = new MetaBall(x, y, z, 1.0, color) {
        @Override
        public double F(M3d v) {
          return forceFunction.F(this.minus(v).length());
        }
      };
      movers.add(this);
      BlobbyDemo.this.registerMouseHandler(this);
      translate(new M3d(x, y, z));
    }
    
    public void update(double t) {
    }

    @Override
    public Mover translate(M3d delta) {
      super.translate(delta);
      metaball.set(getPosition());
      moved = true;
      return this;
    }

    public MetaBall getMetaBall() {
      return metaball;
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private class FunctionFrame extends GLWindowedAppSecondaryFrame {

    public FunctionFrame(GLWindowedApp app) {
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
          getInnerTop() + (int) (getInnerHeight() - 10 - surface.getCutoff() * (getInnerHeight() - 20)),
          getInnerLeft() + getInnerWidth() - 20,
          getInnerTop() + (int) (getInnerHeight() - 10 - surface.getCutoff() * (getInnerHeight() - 20)));
    }
  }
  
  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new BlobbyDemo().run();
  }
}
