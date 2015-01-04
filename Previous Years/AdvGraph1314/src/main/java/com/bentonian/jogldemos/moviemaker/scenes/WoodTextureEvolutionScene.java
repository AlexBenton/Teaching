package com.bentonian.jogldemos.moviemaker.scenes;

import static java.lang.Math.abs;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.advanced.TexturedCube;
import com.bentonian.framework.mesh.textures.IsTextured;
import com.bentonian.framework.mesh.textures.PerlinNoise;
import com.bentonian.framework.mesh.textures.Texture;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class WoodTextureEvolutionScene extends MovieMakerScene {

  private static final M3d OLD_WOOD = new M3d(72, 38, 11).times(1.0 / 255.0);
  private static final M3d NEW_WOOD = new M3d(175, 88, 45).times(1.0 / 255.0);
  private static final PerlinNoise NOISE = new PerlinNoise();

  private WoodenObject A, B, C;

  public WoodTextureEvolutionScene() {
    A = new WoodenObject();
    B = new WoodenObject();
    C = new WoodenObject();

    A.translate(new M3d(-2.05, 0, 0));
    B.translate(new M3d( 0, 0, 0));
    C.translate(new M3d( 2.05, 0, 0));

    add(A);
    add(B);
    add(C);
  }

  @Override
  public void setup(final MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      @Override
      public void onTick(double t) {
        A.tf = t * 2;
        B.nf = t * 2;
        C.na = t * 2;
      }
      @Override public int getEndTick() { return 501; }
    });
  }

  @Override
  public boolean getCaptureRayTracer() {
    return true;
  }

  private static class WoodenObject extends TexturedCube {

    private double tf = 0;
    private double nf = 0;
    private double na = 0;

    public WoodenObject() {
      super(null);
      this.texture = new Texture() {
        @Override
        protected M3d getColor(IsTextured target, M3d pt) {
          double f = (tf + 1) * (pt.getX() * pt.getX() + pt.getZ() * pt.getZ() - abs(pt.getY() + 5) / 5);
          double n = (na + 1) * NOISE.get(pt.times(nf + 1));
          f = f + n;
          f = f - Math.floor(f);
          f = Math.pow(f, 0.5);
          return OLD_WOOD.plus(NEW_WOOD.minus(OLD_WOOD).times(f));
        }
      };
    }
  }
}
