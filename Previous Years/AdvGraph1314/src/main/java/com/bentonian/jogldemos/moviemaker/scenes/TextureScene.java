package com.bentonian.jogldemos.moviemaker.scenes;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.advanced.TexturedSphere;
import com.bentonian.framework.mesh.textures.BumpMappingParametricTexture;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class TextureScene extends MovieMakerScene {

  private final M3d light;
  
  public TextureScene() {
    clearLights();
    light = new M3d(15, 15, 5);
    addLight(light);
    add(new TexturedSphere(BumpMappingParametricTexture.BRICK));
  }

  @Override
  public void setup(final MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      @Override
      public void onTick(double t) {
        light.set(new M3d(-15 * cos(t * 2 * PI), 15 * sin(t * 2 * PI), 5));
      }
      @Override public void done() {
        light.set(new M3d(15, 15, 5));
      }
    });
  }

  @Override
  public boolean getCaptureRayTracer() {
    return true;
  }
  
  @Override
  public int getSupersamplingMultiple() {
    return 4;
  }
}
