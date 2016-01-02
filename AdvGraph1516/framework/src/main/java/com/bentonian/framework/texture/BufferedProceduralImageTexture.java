package com.bentonian.framework.texture;

import com.bentonian.framework.ui.BufferedImageRGBCanvas;

public abstract class BufferedProceduralImageTexture extends BufferedImageTexture {

  @Override
  public void bind() {
    if (textureId == 0) {
      setBufferedImage(BufferedImageRGBCanvas.copyTextureToImage(this, 256, 256));
    }
    super.bind();
  }
}
