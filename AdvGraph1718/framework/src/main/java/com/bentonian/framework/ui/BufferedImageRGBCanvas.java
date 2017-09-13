package com.bentonian.framework.ui;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.texture.Texture;

public class BufferedImageRGBCanvas extends BufferedImage implements RGBCanvas {

  Graphics2D imageAccessor;

  public BufferedImageRGBCanvas(int width, int height) {
    super(width, height, BufferedImage.TYPE_INT_ARGB);
    imageAccessor = createGraphics();
  }

  /**
   * Copy the pixels of the current OpenGL context into a BufferedImage.
   */
  public static BufferedImageRGBCanvas copyOpenGlContextToImage(int sourceWidth, int sourceHeight,
      int destWidth, int destHeight) {

    // Create and fill a ByteBuffer with the frame data.
    ByteBuffer pixels = ByteBuffer.allocateDirect(sourceWidth * sourceHeight * 4);
    GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
    GL11.glReadPixels(0, 0, sourceWidth, sourceHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);

    GL11.glReadBuffer(GL11.GL_BACK);
    return copyPixelsToImage(pixels, sourceWidth, sourceHeight, destWidth, destHeight);
  }

  /**
   * Copy pixels, optionally flipping them vertically.
   */
  public static BufferedImageRGBCanvas copyFrameBufferToImage(GLFrameBuffer framebuffer) {
    int width = framebuffer.getWidth();
    int height = framebuffer.getHeight();

    // Create and fill a ByteBuffer with the frame data.
    ByteBuffer pixels = ByteBuffer.allocateDirect(width * height * 4);
    GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.getTextureId());
    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
    BufferedImageRGBCanvas image = copyPixelsToImage(pixels, width, height, width, height);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    return image;
  }

  private static BufferedImageRGBCanvas copyPixelsToImage(ByteBuffer pixels, int sourceWidth,
      int sourceHeight, int destWidth, int destHeight) {
    final boolean flipVertically = true;

    // Transform the buffer into colored texture pixels
    BufferedImageRGBCanvas image = new BufferedImageRGBCanvas(destWidth, destHeight);
    for (int y = 0; y < destHeight; y++) {
      for (int x = 0; x < destWidth; x++) {
        int i = x * sourceWidth / destWidth;
        int j = (flipVertically ? (destHeight - 1) - y : y) * sourceHeight / destHeight;
        int r = pixels.get(((j * sourceWidth) + i) * 4 + 0) & 0x000000FF;
        int g = pixels.get(((j * sourceWidth) + i) * 4 + 1) & 0x000000FF;
        int b = pixels.get(((j * sourceWidth) + i) * 4 + 2) & 0x000000FF;
        int a = pixels.get(((j * sourceWidth) + i) * 4 + 3) & 0x000000FF;
        image.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | (b << 0));
      }
    }
    return image;

  }

  /**
   * Sample the pixels of the given Texture into a BufferedImage.
   */
  public static BufferedImageRGBCanvas copyTextureToImage(Texture texture, int width, int height) {
    M3d LL = new M3d(-1, -1, 0);
    Square canvas = new Square();
    canvas.setTexture(texture);
    BufferedImageRGBCanvas image = new BufferedImageRGBCanvas(width, height);
    int[] raster = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < width; y++) {
        M3d coord = LL.plus(new M3d(2.0 * x / width, 2.0 * y / height, 0));
        M3d color = texture.getColor(canvas, coord);
        raster[x + (height - 1 - y) * width] = color.asRGBA();
      }
    }
    return image;
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
    imageAccessor.setColor(new Color(1, 1, 1));
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

  private static Color rgb2Color(M3d color) {
    float r = max(min((float) color.get(0), 1), 0);
    float g = max(min((float) color.get(1), 1), 0);
    float b = max(min((float) color.get(2), 1), 0);
    return new Color(r, g, b);
  }
}
