package com.bentonian.jogldemos.moviemaker.scenes;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Circle;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class RefractionScene extends MovieMakerScene {

  private final Sphere sphere;

  public RefractionScene() {
    this.sphere = new Sphere();
    sphere.setRefractiveIndex(1);
    sphere.getMaterial().setLightingCoefficients(0.4, 0.4, 0.8, 2);
    sphere.setTransparency(0.75);
    sphere.scale(new M3d(2,2,2));
    
    add(new Circle().translate(new M3d(0, -2, 0)));
    add(sphere);
  }

  @Override
  public void setup(final MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      @Override
      public void onTick(double t) {
        sphere.setRefractiveIndex(1 + t * 2.0 / 3.0);
      }
      @Override public void done() {
        sphere.setRefractiveIndex(1.1);
      }
    });
  }

  @Override
  public boolean getCaptureRayTracer() {
    return true;
  }

  @Override
  public boolean getAutoReverse() {
    return true;
  }

  @Override
  public M3d getBackground() {
    return SKY_BLUE;
  }
}
