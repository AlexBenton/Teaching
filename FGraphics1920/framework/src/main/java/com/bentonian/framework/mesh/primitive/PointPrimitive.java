package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.ui.GLVertexData;

public class PointPrimitive extends CompiledPrimitive {

  public PointPrimitive() {
    super(new GLVertexData(GLVertexData.Mode.POINTS) {
      @Override
      protected void onDispose() { }
    });
  }
}
