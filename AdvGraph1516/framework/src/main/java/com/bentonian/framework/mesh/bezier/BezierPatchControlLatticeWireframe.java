package com.bentonian.framework.mesh.bezier;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.CompiledPrimitive;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData.Mode;

public class BezierPatchControlLatticeWireframe extends CompiledPrimitive {

  private static final M3d WHITE = new M3d(1, 1, 1);
  
  private final BezierPatch patch;

  public BezierPatchControlLatticeWireframe(BezierPatch patch) {
    super(Mode.LINE_SEGMENTS);
    this.patch = patch;
  }

  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    if (!isCompiled()) {
      color(WHITE);
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          if (i < 3) {
            vertex(patch.getControlPoint(i, j));
            vertex(patch.getControlPoint(i + 1, j));
          }
          if (j < 3) {
            vertex(patch.getControlPoint(i, j));
            vertex(patch.getControlPoint(i, j + 1));
          }
        }
      }
    }
    super.renderLocal(glCanvas);
  }
}
