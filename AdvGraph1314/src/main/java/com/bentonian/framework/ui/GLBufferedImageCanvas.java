package com.bentonian.framework.ui;

import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;

public class GLBufferedImageCanvas extends GLImageBufferCanvas {

  public GLBufferedImageCanvas(int width, int height) {
    super(width, height);
  }

  /**
   * Copy the pixels of the buffer {@code gl} into the texture of this canvas.
   * Assumes that the dimensions of this canvas are identical to those of {@code gl}.
   */
  public void copyContextToCanvas(GL4 gl) {
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
        setRGB(x, (height - 1) - y, (r << 16) | (g << 8) | (b << 0));
      }
    }
  }
}
