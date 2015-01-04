package com.bentonian.jogldemos.moviemaker;

import com.bentonian.framework.math.M3d;
import com.bentonian.raytrace.engine.Scene;

public abstract class MovieMakerScene extends Scene {

  protected static final M3d WHITE = new M3d(1, 1, 1);
  protected static final M3d SKY_BLUE = new M3d(135, 206, 250).times(1.0 / 255.0);

  public MovieMakerScene() {
    addLight(new M3d(20, 20, 20));
  }

  public void setup(MovieMakerBackbone backbone) {
  }

  public void tearDown(MovieMakerBackbone backbone) {
  }

  public String getName() {
    return getClass().getSimpleName().replace("Scene", "");
  }

  public boolean getCaptureRayTracer() {
    return false;
  }

  public boolean getAutoReverse() {
    return false;
  }

  public boolean getLoop() {
    return false;
  }

  public M3d getBackground() {
    return WHITE;
  }

  public int getSupersamplingMultiple() {
    return 4;
  }

  public int getNumShadowRays() {
    return 0;
  }
}
