package com.bentonian.framework.texture;

import static com.bentonian.framework.io.FileUtil.loadImageResource;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.image.BufferedImage;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.ui.GLCanvas;

public class BufferedImageTexture extends Texture {

  public static final BufferedImageTexture AXES = new BufferedImageTexture("axes_3x3.png");
  public static final BufferedImageTexture BRICK = new BufferedImageTexture("brick.jpg");
  public static final BufferedImageTexture STEEL = new BufferedImageTexture("steel.jpg");
  public static final BufferedImageTexture CHECKERBOARD = new BufferedImageTexture("circle-checkerboard.png");

  protected BufferedImage bufferedImage;

  public BufferedImageTexture() {
    this.bufferedImage = null;
  }

  public BufferedImageTexture(BufferedImage bufferedImage) {
    this.bufferedImage = bufferedImage;
  }

  public BufferedImageTexture(int dx, int dy) {
    this(new BufferedImage(dx, dy, BufferedImage.TYPE_INT_ARGB));
  }

  public BufferedImageTexture(String filename) {
    this(BufferedImageTexture.class, filename);
  }

  public BufferedImageTexture(Class<?> clazz, String filename) {
    this(loadImageResource(clazz.getResourceAsStream(filename)));
  }

  public void setBufferedImage(BufferedImage bufferedImage) {
    dispose();
    this.bufferedImage = bufferedImage;
  }

  public BufferedImage getBufferedImage() {
    return bufferedImage;
  }

  @Override
  public void bind() {
    if (textureId == 0) {
      textureId = GLCanvas.setupTexture(bufferedImage);
    }
    super.bind();
  }
  
  @Override
  public Vec3 getColor(IsTextured target, Vec3 pt) {
    TexCoord tc = target.getTextureCoord(pt);
    return Vec3.fromRGBA(sample(tc.u, tc.v));
  }

  @Override
  public double getTransparency(IsTextured target, Vec3 pt) {
    TexCoord tc = target.getTextureCoord(pt);
    int rgba = sample(tc.u, tc.v);
    return 1 - (((rgba >> 24) & 0xFF) / 255.0);
  }

  private int sample(double u, double v) {
    int x = min(bufferedImage.getWidth() - 1, max(0, (int) (bufferedImage.getWidth() * u)));
    int y = min(bufferedImage.getHeight() - 1, max(0, (int) (bufferedImage.getHeight() * v)));
    return bufferedImage.getRGB(x, y);
  }
}
