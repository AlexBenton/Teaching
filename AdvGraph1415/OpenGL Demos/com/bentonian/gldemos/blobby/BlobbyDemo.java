package com.bentonian.gldemos.blobby;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.implicits.ImplicitSurface;
import com.bentonian.framework.mesh.implicits.MetaBall;
import com.bentonian.framework.ui.DemoApp;
import com.google.common.collect.Lists;

public class BlobbyDemo extends DemoApp {

  private static final M3d RED = new M3d(1,0,0);
  private static final M3d BLUE = new M3d(0,0,1);

  private boolean paused = false;
  private double t = 0;
  private ImplicitSurface surface;
  private List<Mover> movers;
  
  // Timing tracking
  long now = 0;
  long then = 0;
  Queue<Long> times = new LinkedList<Long>();

  public BlobbyDemo() {
    super("Blobby Demo");
    this.movers = Lists.newArrayList();
    this.surface = new ImplicitSurface(new M3d(-10,-10,-10), new M3d(10,10,10))
        .setTargetLevel(5)
        .addForce(new Mover(0, 0, 0, 1.0, RED) {
          @Override
          public void update(double t) {
            setX(4*Math.cos(t));
          }
        })
        .addForce(new MetaBall(-4, 0, 0, 1.0, BLUE));
    setCameraDistance(15);
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case Keyboard.KEY_SPACE:
      paused = !paused;
      break;
    case Keyboard.KEY_B:
      surface.setShowBoxes(!surface.getShowBoxes());
      break;
    case Keyboard.KEY_C:
      surface.setBlendColors(!surface.getShowColors());
      break;
    case Keyboard.KEY_E:
      surface.setShowEdges(!surface.getShowEdges());
      break;
    case Keyboard.KEY_EQUALS:
      surface.setTargetLevel(Math.min(10, surface.getTargetLevel() + 1));
      break;
    case Keyboard.KEY_MINUS:
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
      surface.reset();
      surface.refine();
    }
    then = now;
  }

  private void postTick() {
    times.add(now);
    while (now - times.peek() > 1500) {
      times.remove();
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private abstract class Mover extends MetaBall {

    public Mover(double x, double y, double z, double strength, M3d color) {
      super(x, y, z, strength, color);
      movers.add(this);
    }

    public abstract void update(double t);
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new BlobbyDemo().run();
  }
}
