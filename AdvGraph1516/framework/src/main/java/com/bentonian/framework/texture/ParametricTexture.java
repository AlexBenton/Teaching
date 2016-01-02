package com.bentonian.framework.texture;

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.floor;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.BufferedImageRGBCanvas;

public class ParametricTexture extends BufferedProceduralImageTexture {
  
  public ParametricTexture() {
    setBufferedImage(BufferedImageRGBCanvas.copyTextureToImage(this, 256, 256));
  }

  public static final Texture CHECKERBOARD = new ParametricTexture() {
    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      TexCoord tc = target.getTextureCoord(pt);
      int x = (int)(floor(tc.u * 5));
      int y = (int)(floor(tc.v * 5));
      return (((x + y) & 0x01) == 0) ? BLACK : WHITE;
    }
  };

  public static final Texture SPIRAL = new ParametricTexture() {
    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      TexCoord tc = target.getTextureCoord(pt);
      double theta = 6 * atan2(tc.v - 0.5, tc.u - 0.5);
      double r = sqrt((tc.u - 0.5) * (tc.u - 0.5) + (tc.v - 0.5) * (tc.v - 0.5));
      theta += (r+1) * (r+1) * PI;

      return GREEN
          .plus(RED.times((cos(theta) + 1) / 2))
          .plus(BLUE.times((sin(theta) + 1) / 2))
          .times(0.75);
    }
  };

  public static final Texture BRICK = new ParametricTexture() {
    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      TexCoord tc = target.getTextureCoord(pt);
      int tx = (int) (10 * tc.u);
      int ty = (int) (10 * tc.v);
      boolean oddity = (tx & 0x01) == (ty & 0x01);
      boolean isEdge = ((10 * tc.u - tx < 0.1) && oddity) || (10 * tc.v - ty < 0.1);
      return isEdge ? WHITE : RED;
    }
  };
}
