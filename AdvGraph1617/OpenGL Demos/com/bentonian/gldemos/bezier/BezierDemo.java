package com.bentonian.gldemos.bezier;

import com.bentonian.framework.mesh.bezier.BezierPatch;
import com.bentonian.framework.mesh.bezier.BezierPatchControlLattice;
import com.bentonian.framework.ui.DemoApp;

public class BezierDemo extends DemoApp {

  private final BezierPatch patch;
  private final BezierPatchControlLattice controller;

  protected BezierDemo() {
    super("Bezier patch");
    this.patch = new BezierPatch();
    this.controller = new BezierPatchControlLattice(this, patch);
    setCameraDistance(4);
    patch.getFeaturesAccelerator().setShowEdges(true);
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
