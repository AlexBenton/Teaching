package com.bentonian.jogldemos.moviemaker.scenes;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.Circle;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;
import com.bentonian.raytrace.csg.Difference;

public class DifferenceScene extends MovieMakerScene {

  private final Primitive cube;
  private final Primitive cubeEcho;

  public DifferenceScene() {
    this.cube = new Cube(new M3d(0.2, 1, 0.2))
        .scale(new M3d(1.5, 1.5, 1.5));
    this.cubeEcho = new Cube(new M3d(0.2, 1, 0.2))
        .setTransparency(0.9)
        .scale(new M3d(1.4999, 1.4999, 1.4999));

    Primitive sphere = new Sphere(new M3d(0.2, 0.2, 1))
        .setLightingCoefficients(0.4, 0.6, 0.4, 1)
        .scale(new M3d(1.4, 1.4, 1.4))
        .scale(new M3d(1.5, 1.5, 1.5));

    add(new Circle()
        .setReflectivity(0.75)
        .translate(new M3d(0, -2, 0)));
    add(new Difference(sphere, cube));
    add(cubeEcho);
  }

  @Override
  public void setup(final MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      @Override
      public void onTick(double t) {
        cube.setIdentity().translate(new M3d(0, 0, 5 * t - 2.5)).scale(new M3d(1.5, 1.5, 1.5));
        cubeEcho.setIdentity().translate(new M3d(0, 0, 5 * t - 2.5)).scale(new M3d(1.4999, 1.4999, 1.4999));
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
}
