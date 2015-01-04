package com.bentonian.jogldemos.moviemaker.scenes;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import java.awt.Dimension;

import com.bentonian.framework.material.MaterialPrimitive;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.framework.ui.GLVertexData;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class MorphScene extends MovieMakerScene {

  private Surface surface;
  private Dimension editorSize;
  
  public MorphScene() {
    this.surface = new Surface();
    add(surface);
  }

  @Override
  public void setup(final MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      @Override public void start() {
        editorSize = backbone.getEditor().getImageSize();
        backbone.getEditor().setImageSize(640, 640);
      }
      @Override public void done() {
        if (editorSize != null) {
          backbone.getEditor().setImageSize(editorSize.width, editorSize.height);
          editorSize = null;
        }
      }
      @Override public int getEndTick() { return 200; }
      @Override public void onTick(double t) {
        surface.progress = t;
      }
    });
  }

  ////////////////////////

  class Surface extends MaterialPrimitive {

    private double progress = 0;
    private MeshPt[][] mesh;

    @Override
    protected void renderLocal(GLRenderingContext glCanvas) {
      buildMesh();
      
      double t = progress * 2 * PI;
      glCanvas.push(
          M4x4.rotationMatrix(new M3d(1.0f, 0.0f, 0.0f), t).times(
          M4x4.rotationMatrix(new M3d(0.0f, 1.0f, 0.0f), t).times(
          M4x4.rotationMatrix(new M3d(0.0f, 0.0f, 1.0f), t).times(
          M4x4.rotationMatrix(new M3d(0.0f, 1.0f, 0.0f), t)))));

      GLVertexData vao = GLVertexData.beginQuads();
      for (int i = 0; i < DI - 1; i++) {
        for (int j = 0; j < DJ - 1; j++) {
          for (int[] coord : SQUARE) {
            vao.normal(getMesh(i + coord[0], j + coord[1]).getNormal());
            vao.color(getMesh(i + coord[0], j + coord[1]).getColor());
            vao.vertex(getMesh(i + coord[0], j + coord[1]));
          }
        }
      }
      vao.render(glCanvas).dispose();

      vao = GLVertexData.beginLineQuads();
      vao.color(BLACK);
      for (int i = 0; i < DI - 1; i++) {
        for (int j = 0; j < DJ - 1; j++) {
          for (int[] coord : SQUARE) {
            vao.vertex(getMesh(i + coord[0], j + coord[1]));
          }
        }
      }
      vao.render(glCanvas).dispose();

      glCanvas.pop();
    }

    public void buildMesh() {
      int which = (int) (progress * GENERATORS.length);
      double progressToNextGenerator = (progress * GENERATORS.length) - which;
      double t = sin(progressToNextGenerator * (PI / 2));
      Generator A = GENERATORS[which % GENERATORS.length];
      Generator B = GENERATORS[(which + 1) % GENERATORS.length];

      mesh = new MeshPt[DI][DJ];
      for (int i = 0; i<DI; i++) {
        for (int j = 0; j<DJ; j++) {
          double u = (double)i / (DI-1);
          double v = (double)j / (DJ-1);
          mesh[i][j] = new MeshPt(A.get(u, v).times(1-t).plus(B.get(u, v).times(t)), i, j);
        }
      }
    }

    public MeshPt getMesh(int i, int j) {
      while (i < 0) { i += DI; };
      while (j < 0) { j += DJ; };
      return mesh[i % DI][j % DJ];
    }

    ///////////////////////////////////////////////////////////////////////////

    class MeshPt extends M3d {
      private final int i, j;

      MeshPt(M3d pt, int i, int j) {
        super(pt);
        this.i = i;
        this.j = j;
      }

      MeshPt getAdjacent(int deltai, int deltaj) { return getMesh(i + deltai, j + deltaj); }
      M3d getColor() { return new M3d((get(0) + 1) / 2, (get(1) + 1) / 2, (get(2) + 1) / 2); }
      M3d getNormal() {
        M3d a = getAdjacent(-1, 0).minus(this);
        M3d b = getAdjacent(0, -1).minus(this);
        M3d c = getAdjacent(1, 0).minus(this);
        M3d d = getAdjacent(0, 1).minus(this);
        M3d ab = a.cross(b);
        M3d bc = b.cross(c);
        M3d cd = c.cross(d);
        M3d da = d.cross(a);
        M3d n = ab.plus(bc).plus(cd).plus(da).normalized();
        return n;
      }
    }
  }

  ///////////////////////////////////////////////////////////////////////////

  private static interface Generator {
    M3d get(double u, double v);
  }

  private static final Generator SPHERE = new Generator() {
    @Override
    public M3d get(double u, double v) {
      u = 2 * PI * u;
      v = PI * (v-0.5);
      return new M3d(Math.cos(u) * Math.cos(v),
                     Math.sin(u) * Math.cos(v),
                     Math.sin(v));
    }
  };

  private static final Generator POWER_SPHERE = new Generator() {
    @Override
    public M3d get(double u, double v) {
      u = 2 * PI * u;
      v = PI * (v-0.5);
      double x = Math.cos(u) * Math.cos(v);
      double y = Math.sin(u) * Math.cos(v);
      double z = Math.sin(v);

      return new M3d(x*x*x, y*y*y, z*z*z);
    }
  };

  private static final Generator CYLINDER = new Generator() {
    @Override
    public M3d get(double u, double v) {
      u = 2 * PI * u;
      v = 2 * (v-0.5);
      return new M3d(Math.cos(u),
                     sin(u),
                     v);
    }
  };

  private static final Generator TORUS = new Generator() {
    @Override
    public M3d get(double u, double v) {
      M3d pt;

      u = 2 * PI * u;
      v = 2 * PI * v + PI;
      pt = new M3d(1+0.25*cos(v), 0, 0.25*sin(v));
      return new M3d(pt.get(0) * cos(u) - pt.get(1) * sin(u),
                     pt.get(0) * sin(u) + pt.get(1) * cos(u),
                     pt.get(2));
    }
  };
    
  private static final int DI = 32;
  private static final int DJ = 32;
  private static final M3d BLACK = new M3d(0, 0, 0);  
  private static final int[][] SQUARE = new int[][] { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } };
  private static final Generator[] GENERATORS = { SPHERE, POWER_SPHERE, CYLINDER, TORUS };
}
