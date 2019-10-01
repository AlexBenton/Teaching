package com.bentonian.framework.texture;

import com.bentonian.framework.math.Vec3;

public interface IsTextured {

  public TexCoord getTextureCoord(Vec3 pt);
  public Vec3 getUBasis(Vec3 pt);
}
