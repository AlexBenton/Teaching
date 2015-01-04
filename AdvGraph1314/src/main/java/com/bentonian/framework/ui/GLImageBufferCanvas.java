package com.bentonian.framework.ui;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.media.opengl.GL;
import javax.media.opengl.GL4;

import com.bentonian.framework.math.M3d;

public class GLImageBufferCanvas extends BufferedImage implements RGBCanvas {

  Graphics2D imageAccessor;

  public GLImageBufferCanvas(int width, int height) {
    super(width, height, BufferedImage.TYPE_3BYTE_BGR);
    imageAccessor = createGraphics();
  }

  @Override
  public void putPixel(int x, int y, M3d color) {
    setRGB(x, y, rgb2Color(color).getRGB());
  }

  @Override
  public void fill(double x, double y, double dx, double dy, M3d color) {
    imageAccessor.setColor(rgb2Color(color));
    imageAccessor.fillRect((int) x, (int) y, (int) dx, (int) dy);
  }

  public void clear() {
    imageAccessor.setColor(new Color(1,1,1));
    imageAccessor.fillRect(0, 0, getWidth(), getHeight());
  }

  /**
   * Saves the current image to {@code filename}.
   */
  public void write(String filename) {
    try {
      ImageIO.write(this, "png", new File(filename));
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }

  /**
   * Saves the framebuffer of context {@code gl} to {@code filename}
   */
  public void write(GL4 gl, String filename) {
    copyOpenGlContextToImage(gl);
    write(filename);
  }

  /**
   * Copy the pixels of the buffer {@code gl} into the texture of this canvas.
   * Assumes that the dimensions of {@code image} are identical to those of {@code gl}.
   */
  public void copyOpenGlContextToImage(GL4 gl) {
    int width = getWidth();
    int height = getHeight();

    // Create and fill a ByteBuffer with the frame data.
    ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 3 );
    gl.glReadBuffer(GL.GL_BACK);
    gl.glPixelStorei(GL.GL_PACK_ALIGNMENT, 1);
    gl.glReadPixels(0, 0, width, height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixels);

    // Transform the buffer into colored texture pixels
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int r = pixels.get(((y * width) + x) * 3 + 0) & 0x000000FF;
        int g = pixels.get(((y * width) + x) * 3 + 1) & 0x000000FF;
        int b = pixels.get(((y * width) + x) * 3 + 2) & 0x000000FF;
        setRGB(x, y, (r << 16) | (g << 8) | (b << 0));
      }
    }
  }

  private static Color rgb2Color(M3d color) {
    float r = max(min((float)color.get(0), 1), 0);
    float g = max(min((float)color.get(1), 1), 0);
    float b = max(min((float)color.get(2), 1), 0);
    return new Color(r, g, b);
  }
}
