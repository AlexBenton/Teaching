package com.bentonian.framework.mesh.textures;

import static com.bentonian.framework.io.FileUtil.loadImageResource;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.advanced.TexturedSquare;
import com.bentonian.framework.ui.GLRenderingContext;

public class ImageTexture extends Texture {

  public static final ImageTexture BRICK = new ImageTexture("brick.jpg");
  public static final ImageTexture STEEL = new ImageTexture("steel.jpg");

  private final BufferedImage image;
  private final int width;
  private final int height;

  private int textureId;

  public ImageTexture(String filename) {
    this(loadImageResource(ImageTexture.class.getResourceAsStream(filename)));
  }

  public ImageTexture(Texture source, int dx, int dy) {
    this(copy(source, dx, dy));
  }

  public ImageTexture(BufferedImage texture) {
    this.image = texture;
    this.width = texture.getWidth();
    this.height = texture.getHeight();
    this.textureId = 0;
  }

  @Override
  protected M3d getColor(IsTextured target, M3d pt) {
    TexCoord tc = target.getTextureCoord(pt);
    return sample(tc.u, tc.v);
  }

  @Override
  public int getOpenGlTextureId(GLRenderingContext glCanvas) {
    if (textureId == 0) {
      textureId = glCanvas.setupTexture(image);
    }
    return textureId;
  }

  private M3d sample(double u, double v) {
    int x = min(width - 1, max(0, (int) (width * u)));
    int y = min(height - 1, max(0, (int) (height * v)));
    int rgb = image.getRGB(x, y);
    int r = (rgb >> 16) & 0xFF;
    int g = (rgb >> 8) & 0xFF;
    int b = (rgb >> 0) & 0xFF;
    return new M3d(r, g, b).times(1.0 / 255.0);
  }

  private static BufferedImage copy(Texture source, int dx, int dy) {
    M3d LL = new M3d(-1, -1, 0);
    TexturedSquare canvas = new TexturedSquare(source);
    BufferedImage image = new BufferedImage(dx, dy, BufferedImage.TYPE_INT_ARGB);
    int[] raster = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
    if (source != null) {
      for (int x = 0; x < dx; x++) {
        for (int y = 0; y < dx; y++) {
          M3d coord = LL.plus(new M3d(2.0 * x / dx, 2.0 * y / dy, 0));
          M3d color = source.getColor(canvas, coord);
          raster[x + (dy - 1 - y) * dx] = color.asRGBA();
        }
      }
    }
    return image;
  }
}
