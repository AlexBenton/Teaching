package com.bentonian.framework.texture;

import com.bentonian.framework.math.M3d;

public interface IsTextured {

  public TexCoord getTextureCoord(M3d pt);
  public M3d getUBasis(M3d pt);
}
