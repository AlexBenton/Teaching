package com.bentonian.framework.mesh.subdivision;

import com.bentonian.framework.mesh.Mesh;

public interface SubdivisionFunction {

  public abstract Mesh apply(Mesh mesh);
}
