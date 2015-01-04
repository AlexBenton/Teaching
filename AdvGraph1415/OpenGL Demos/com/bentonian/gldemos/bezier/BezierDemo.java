package com.bentonian.gldemos.bezier;

import com.bentonian.framework.mesh.bezier.BezierPatch;
import com.bentonian.framework.mesh.bezier.BezierPatchControlLattice;
import com.bentonian.framework.ui.DemoApp;

public class BezierDemo extends DemoApp {

  private final BezierPatch patch = new BezierPatch();
  private final BezierPatchControlLattice controller = new BezierPatchControlLattice(patch);

  protected BezierDemo() {
    super("Bezier patch");
    setCameraDistance(4);
    patch.getFeaturesAccelerator().setShowEdges(true);
    registerMouseHandler(controller);
  }

  @Override
  public void draw() {
    patch.render(this);
    controller.render(this);
  }

  public static void main(String[] args) {
    new BezierDemo().run();
  }
}
