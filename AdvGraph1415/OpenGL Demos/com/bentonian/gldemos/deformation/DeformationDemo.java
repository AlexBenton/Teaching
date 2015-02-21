package com.bentonian.gldemos.deformation;

import org.lwjgl.input.Keyboard;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.implicits.ImplicitSurface;
import com.bentonian.framework.mesh.implicits.MetaStrip;
import com.bentonian.framework.mesh.implicits.TwistDistortion;
import com.bentonian.framework.ui.DemoApp;

public class DeformationDemo extends DemoApp {

  private final ImplicitSurface surface;
  private final TwistDistortion twist;
  
  public DeformationDemo() {
    super("Deformation Demo");
    this.twist = new TwistDistortion(new MetaStrip(new M3d(0.2, 5, 1)));
    this.surface = new ImplicitSurface(new M3d(-10,-10,-10), new M3d(10,10,10))
        .setTargetLevel(6)
        .addForce(twist);
    setCameraDistance(10);
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case Keyboard.KEY_E:
      surface.setShowEdges(!surface.getShowEdges());
      break;
    case Keyboard.KEY_EQUALS:
      surface.setTargetLevel(Math.min(10, surface.getTargetLevel() + 1));
      break;
    case Keyboard.KEY_MINUS:
      surface.setTargetLevel(Math.max(1, surface.getTargetLevel() - 1));
      break;
    case Keyboard.KEY_LBRACKET:
      twist.setTwist(twist.getTwist() - 0.25);
      surface.reset();
      break;
    case Keyboard.KEY_RBRACKET:
      twist.setTwist(twist.getTwist() + 0.25);
      surface.reset();
      break;
    default:
      super.onKeyDown(key);
      break;
    }
  }

  @Override
  protected void draw() {
    surface.render(this);
    setTitle("Deformation Demo - Level = " + surface.getTargetLevel()
        + ", twist = " + twist.getTwist() + ", polys = " + surface.getNumPolys());
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new DeformationDemo().run();
  }
}
