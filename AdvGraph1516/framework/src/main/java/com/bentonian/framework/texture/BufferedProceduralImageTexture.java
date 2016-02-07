package com.bentonian.framework.texture;

import com.bentonian.framework.ui.BufferedImageRGBCanvas;

public abstract class BufferedProceduralImageTexture extends BufferedImageTexture {

  @Override
  public void bind() {
    if (textureId == 0) {
      setBufferedImage(BufferedImageRGBCanvas.copyTextureToImage(
          this, getBufferedImageWidth(), getBufferedImageHeight()));
    }
    super.bind();
  }
  
  protected int getBufferedImageWidth() {
    return 256;
  }
  
  protected int getBufferedImageHeight() {
    return 256;
  }
}
