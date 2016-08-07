package com.bentonian.framework.texture;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.BufferedImageRGBCanvas;
import com.bentonian.framework.ui.GLCanvas;

public abstract class DynamicTexture extends Texture {

  public static final Texture SPIRAL = new DynamicTexture() {

    private long tick = 0;
    private double t = 0;

    @Override
    public void bind() {
      super.bind();
      long tock = System.currentTimeMillis();
      if (tick > 0) {
        t += (tock - tick) / 1000.0;
      }
      tick = tock;
      GLCanvas.updateTexture(textureId, BufferedImageRGBCanvas.copyTextureToImage(this, 256, 256));
    }

    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      TexCoord tc = target.getTextureCoord(pt);
      double theta = t + 6 * atan2(tc.v - 0.5, tc.u - 0.5);
      double r = sqrt((tc.u - 0.5) * (tc.u - 0.5) + (tc.v - 0.5) * (tc.v - 0.5));
      theta += (r+1) * (r+1) * PI;

      return WHITE.times((cos(theta) + 1) / 2)
          .plus(BLUE.times((sin(theta) + 1) / 2));
    }
  };
}
