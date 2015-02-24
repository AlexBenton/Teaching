package com.bentonian.gldemos.blobby;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.lwjgl.input.Keyboard;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.implicits.ImplicitSurface;
import com.bentonian.framework.mesh.implicits.MetaBall;
import com.bentonian.framework.scene.ControlWidget;
import com.bentonian.framework.ui.DemoApp;
import com.google.common.collect.Lists;

public class BlobbyDemo extends DemoApp {

  private static final M3d RED = new M3d(1,0,0);
  private static final M3d BLUE = new M3d(0,0,1);

  private boolean paused = false;
  private boolean moved = false;
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
    this.surface = new ImplicitSurface(new M3d(-8,-8,-8), new M3d(8,8,8))
        .setTargetLevel(5)
        .addForce(new Mover(
            6.7841952392351645, 2.7370054894176192, -1.7763568394002505E-15,
            1.0, RED) {
          @Override
          public void update(double t) {
            translate(new M3d(4 * Math.cos(t), 0, 0).minus(getPosition()));
          }
        }.getMetaBall())
        .addForce(new Mover(-4, 0, 0, 1.0, BLUE).getMetaBall())
        .refineCompletely();
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
      surface.dispose();
      break;
    case Keyboard.KEY_E:
      surface.setShowEdges(!surface.getShowEdges());
      break;
    case Keyboard.KEY_F:
      surface.setShowFaces(!surface.getShowFaces());
      break;
    case Keyboard.KEY_EQUALS:
      surface.setTargetLevel(Math.min(10, surface.getTargetLevel() + 1));
      surface.dispose();
      break;
    case Keyboard.KEY_MINUS:
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
      setTitle("Blobby Demo: " + surface.getNumPolys() + " polys @ "
          + String.format("%.02f", times.size() / delta) + " fps");
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private class Mover extends ControlWidget {
    final MetaBall metaball;

    public Mover(double x, double y, double z, double strength, M3d color) {
      metaball = new MetaBall(x, y, z, strength, color);
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

  public static void main(String[] args) {
    new BlobbyDemo().run();
  }
}
