package com.bentonian.framework.mesh.bezier;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.scene.ControlWidget;
import com.bentonian.framework.scene.PrimitiveCollection;
import com.bentonian.framework.ui.GLWindowedCanvas;

public class BezierPatchControlLattice extends PrimitiveCollection {

  private final BezierPatch patch;
  private final BezierPatchControlLatticeWireframe latticeWireframe;

  public BezierPatchControlLattice(GLWindowedCanvas canvas, BezierPatch patch) {
    this.patch = patch;
    this.latticeWireframe = new BezierPatchControlLatticeWireframe(patch); 

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        LatticeControlWidget control = new LatticeControlWidget();
        control.i = i;
        control.j = j;
        control.scale(0.05);
        control.translate(patch.getControlPoint(i, j));
        canvas.registerMouseHandler(control);
        add(control);
      }
    }
    add(latticeWireframe);
  }

  private class LatticeControlWidget extends ControlWidget {
    int i, j;
    
    @Override
    public LatticeControlWidget translate(Vec3 delta) {
      super.translate(delta);
      patch.getControlPoint(i, j).set(getPosition());
      patch.dispose();
      latticeWireframe.dispose();
      return this;
    }
  }
}
