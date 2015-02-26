package com.bentonian.framework.mesh.bezier;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshVertex;
import com.bentonian.framework.mesh.primitive.MeshPrimitive;
import com.bentonian.framework.ui.GLCanvas;

public class BezierPatch extends MeshPrimitive {

  public static final int DU = 10;
  public static final int DV = 10;
  private static final int[][] FACE_OFFSETS = { { 0, 0 }, { 1, 0 }, { 1, 1 }, { 0, 1 } };

  private final M3d[][] P = new M3d[4][4];
  private final MeshVertex[][] meshVertices = new MeshVertex[DU + 1][DV + 1];

  public BezierPatch() {
    super(new Mesh());

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        P[i][j] = new M3d(-1.5 + i, -1.5 + j, 0);
      }
    }
    for (int u = 0; u <= DU; u++) {
      for (int v = 0; v <= DV; v++) {
        meshVertices[u][v] = new MeshVertex(u, v, 0);  // Placeholder values
      }
    }
    for (int u = 0; u < DU; u++) {
      for (int v = 0; v < DV; v++) {
        MeshVertex faceVerts[] = new MeshVertex[4];
        for (int k = 0; k < 4; k++) {
          int[] offset = FACE_OFFSETS[k];
          faceVerts[k] = meshVertices[u + offset[0]][v + offset[1]];
        }
        getMesh().add(new MeshFace(faceVerts));
      }
    }
  }
  
  public M3d getControlPoint(int i, int j) {
    return P[i][j];
  }
  
  public MeshVertex getMeshVertex(int u, int v) {
    return meshVertices[u][v];
  }
  
  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    if (!isCompiled()) {
      double du = 1.0 / DU;
      double dv = 1.0 / DV;
      
      for (int u = 0; u <= DU; u++) {
        for (int v = 0; v <= DV; v++) {
          meshVertices[u][v].set(cubicBezier(u * du, v * dv));
        }
      }
      getMesh().computeAllNormals();
    }
    super.renderLocal(glCanvas);
  }

  private M3d cubicBezier(double u, double v) {
    M3d P = new M3d();
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        P = P.plus(pt(i, j, u, v));
      }
    }
    return P;
  }

  private M3d pt(int i, int j, double u, double v) {
    return P[i][j].times(spline(i, u) * spline(j, v));
  }

  private double spline(int i, double t) {
    switch (i) {
    case 0: return cube(1 - t);
    case 1: return 3 * t * sqr(1 - t);
    case 2: return 3 * sqr(t) * (1 - t);
    case 3: return cube(t);
    default: return 0;
    }
  }

  private static double cube(double d) {
    return d * d * d;
  }

  private static double sqr(double d) {
    return d * d;
  }
}
