package com.bentonian.jogldemos.blobby;

import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.metaballs.ImplicitSurface;
import com.bentonian.framework.mesh.metaballs.MetaBall;
import com.bentonian.jogldemos.internals.JoglDemo;
import com.bentonian.jogldemos.internals.JoglDemoContainer;
import com.google.common.collect.Lists;

public class BlobbyDemo extends JoglDemo {

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
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case KeyEvent.VK_SPACE:
      paused = !paused;
      break;
    case KeyEvent.VK_B:
      surface.setShowBoxes(!surface.getShowBoxes());
      break;
    case KeyEvent.VK_C:
      surface.setBlendColors(!surface.getShowColors());
      break;
    case KeyEvent.VK_E:
      surface.setShowEdges(!surface.getShowEdges());
      break;
    case KeyEvent.VK_PLUS:
    case KeyEvent.VK_EQUALS:
      surface.setTargetLevel(Math.min(10, surface.getTargetLevel() + 1));
      break;
    case KeyEvent.VK_MINUS:
    case KeyEvent.VK_UNDERSCORE:
      surface.setTargetLevel(Math.max(1, surface.getTargetLevel() - 1));
      break;
    default: super.keyPressed(e);
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
    JoglDemoContainer.go(new BlobbyDemo());
  }
}
