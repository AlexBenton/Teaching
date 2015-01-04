package com.bentonian.framework.texture;

import com.bentonian.framework.opencv.Webcam;

public class WebcamTexture extends DynamicTexture {

  private final Webcam webcam;

  public WebcamTexture(int webcamIndex) {
    this.webcam = new Webcam(webcamIndex);
  }

  public float getAspect() {
    return (float) webcam.getCols() / (float) webcam.getRows();
  }

  public Webcam getWebcam() {
    return webcam;
  }

  @Override
  public void bind() {
    super.bind();
    webcam.bind(textureId);
  }
  
  @Override
  public void dispose() {
    webcam.dispose();
    super.dispose();
  }
}
