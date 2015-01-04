package com.bentonian.jogldemos.moviemaker.scenes;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.metaballs.ImplicitSurface;
import com.bentonian.framework.mesh.metaballs.MetaStrip;
import com.bentonian.framework.mesh.metaballs.TwistDistortion;
import com.bentonian.framework.mesh.primitive.Circle;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class TwistScene extends MovieMakerScene {

  private final TwistDistortion stripTwist;
  private final ImplicitSurface stripImplicit;
  
  public TwistScene() {
    add(new Circle().translate(new M3d(0, -2, 0)));
//    add(new Circle().translate(new M3d(0, -2, 0)).rotate(new M3d(0,0,1), Math.PI/2));

    this.stripTwist = new TwistDistortion(new MetaStrip(new M3d(0.2, 5, 1)));
    this.stripImplicit = new ImplicitSurface(new M3d(-5,-5,-5), new M3d(5,5,5));

    stripTwist.setTwist(1);
    stripImplicit
        .setTargetLevel(5)
        .addForce(stripTwist)
        .resetAndRefine();

    add(stripImplicit
        .setShowEdges(false)
        .setBlendColors(true)
        .setReflectivity(0.5));
  }

  @Override
  public void setup(final MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      @Override public int getEndTick() { return 500; }
      @Override
      public void start() {
        stripImplicit.setTargetLevel(8);
      }
      @Override
      public void onTick(double t) {
        stripTwist.setTwist(t * t * (3 - 2 * t));  // Ease curve
        stripImplicit.resetAndRefine();
      }
      @Override
      public void done() {
        stripImplicit.setTargetLevel(5);
        stripTwist.setTwist(1);
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
