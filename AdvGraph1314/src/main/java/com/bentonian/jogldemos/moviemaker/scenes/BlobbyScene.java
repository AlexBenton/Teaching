package com.bentonian.jogldemos.moviemaker.scenes;

import static java.lang.Math.PI;
import static java.lang.Math.cos;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.metaballs.ImplicitSurface;
import com.bentonian.framework.mesh.metaballs.MetaBall;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class BlobbyScene extends MovieMakerScene {

  private final MetaBall A;
  private final MetaBall B;
  private final ImplicitSurface surface;
  
  public BlobbyScene() {
    this.A = new MetaBall(-3, 0, 0, 1.1, new M3d(0.2, 1, 0.2));
    this.B = new MetaBall(3, 0, 0, 1.1, new M3d(0.2, 0.2, 1));
    this.surface = new ImplicitSurface(new M3d(-5, -5, -5), new M3d(5,5,5))
        .addForce(A)
        .addForce(B)
        .setTargetLevel(5)
        .setShowEdges(false);
    add(surface);
  }

  @Override
  public void setup(MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      @Override public int getEndTick() { return 100; }
      @Override public void onTick(double t) {
        A.setX(3 * cos(t * PI));
        B.setX(-3 * cos(t * PI));
        surface.resetAndRefine();
      }
    });
  }
}
