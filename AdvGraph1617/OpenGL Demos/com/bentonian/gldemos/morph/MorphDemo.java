package com.bentonian.gldemos.morph;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import org.lwjgl.glfw.GLFW;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLVertexData;


public class MorphDemo extends DemoApp {
  private static final int di = 32;
  private static final int dj = 32;

  private int tick = 0;
  private MeshPt[][] meshPoints;
  private boolean paused = false;

  public MorphDemo() {
    super("Parametric Morph");
    setCameraDistance(3);
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case GLFW.GLFW_KEY_SPACE:
      paused = !paused;
      break;
    default:
      super.onKeyDown(key);
      break;
    }
  }

  ////////////////////////

  class MeshPt extends M3d {
    private final int i, j;

    MeshPt(M3d pt, int i, int j) { super(pt); this.i = i; this.j = j; }

    MeshPt getAdjacent(int deltai, int deltaj) { return getMesh(i+deltai,j+deltaj); }
    MeshPt vertex(GLVertexData vao) { vao.vertex(this); return this; }
    MeshPt color(GLVertexData vao) { vao.color(new M3d((get(0)+1)/2.0, (get(1)+1)/2.0, (get(2)+1)/2.0)); return this; }
    MeshPt normal(GLVertexData vao) {
      M3d a = getAdjacent(-1, 0).minus(this);
      M3d b = getAdjacent(0, -1).minus(this);
      M3d c = getAdjacent(1, 0).minus(this);
      M3d d = getAdjacent(0, 1).minus(this);
      M3d ab = a.cross(b);
      M3d bc = b.cross(c);
      M3d cd = c.cross(d);
      M3d da = d.cross(a);
      M3d n = ab.plus(bc).plus(cd).plus(da).normalized();

      vao.normal(n);
      return this;
    }
  }

  ////////////////////////

  interface Generator {
    M3d get(double u, double v);
  }

  ////////////////////////

  Generator sphere = new Generator() {
    @Override
    public M3d get(double u, double v) {
      u = 2 * PI * u;
      v = PI * (v-0.5);
      return new M3d(Math.cos(u) * Math.cos(v),
          Math.sin(u) * Math.cos(v),
          Math.sin(v));
    } 
  };

  Generator powerSphere = new Generator() { 
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

  Generator cylinder = new Generator() { 
    @Override
    public M3d get(double u, double v) {
      u = 2 * PI * u;
      v = 2 * (v-0.5);
      return new M3d(Math.cos(u),
          sin(u),
          v);
    } 
  };

  Generator torus = new Generator() { 
    @Override
    public M3d get(double u, double v) {
      u = 2 * PI * u;
      v = 2 * PI * v + PI;
      M3d pt = new M3d(1+0.25*cos(v), 0, 0.25*sin(v));
      return new M3d(pt.get(0) * cos(u) - pt.get(1) * sin(u),
          pt.get(0) * sin(u) + pt.get(1) * cos(u),
          pt.get(2));
    }
  };

  public void buildMesh() {
    Generator[] generators = { sphere, powerSphere, cylinder, torus };
    int which = tick/500;
    double t = sin(((double)(tick%500))*(PI/2.0)/500.0);
    Generator A = generators[which%generators.length];
    Generator B = generators[(which+1)%generators.length];

    meshPoints = new MeshPt[di][dj];
    for (int i = 0; i<di; i++) {
      for (int j = 0; j<dj; j++) {
        double u = (double)i / (di-1);
        double v = (double)j / (dj-1);
        meshPoints[i][j] = new MeshPt(A.get(u, v).times(1-t).plus(B.get(u, v).times(t)), i, j);
      }
    }
  }

  public MeshPt getMesh(int i, int j) {
    while (i < 0) { i += di; };
    while (j < 0) { j += dj; };
    return meshPoints[i % di][j % dj];
  }

  @Override
  public void draw() {
    float t = ((float) tick) / 50.0f;
    buildMesh();

    push(
        M4x4.rotationMatrix(new M3d(1.0f, 0.0f, 0.0f), t).times(
        M4x4.rotationMatrix(new M3d(0.0f, 1.0f, 0.0f), t).times(
        M4x4.rotationMatrix(new M3d(0.0f, 0.0f, 1.0f), t).times(
        M4x4.rotationMatrix(new M3d(0.0f, 1.0f, 0.0f), t)))));

    GLVertexData vao = GLVertexData.beginQuads();
    for (int i = 0; i<di-1; i++) {
      for (int j = 0; j<dj-1; j++) {
        for (int[] step : new int[][] { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } }) {
          getMesh(i + step[0], j + step[1]).normal(vao).color(vao).vertex(vao);
        }
      }
    }
    vao.render(this);
    vao.dispose();

    vao = GLVertexData.beginLineQuads();
    vao.color(new M3d(0, 0, 0));
    for (int i = 0; i < di - 1; i++) {
      for (int j = 0; j < dj - 1; j++) {
        for (int[] step : new int[][] { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } }) {
          getMesh(i + step[0], j + step[1]).vertex(vao);
        }
      }
    }
    vao.render(this);
    vao.dispose();

    if (!paused) {
      tick++;
    }
    pop();
  }

  public static void main(String[] args) {
    new MorphDemo().run();
  }
}
