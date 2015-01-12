package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Edge;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;
import com.bentonian.framework.ui.GLVertexData.Mode;

public class MeshPrimitiveFeatureAccelerator {
  
  private static final M3d BLACK = new M3d(0, 0, 0);
  private static final M3d RED = new M3d(1, 0, 0);
  
  private final Mesh mesh;
  private final GLVertexData vao;
  
  private M3d edgeColor = BLACK;
  private boolean showEdges = false;
  private boolean showNormals = false;

  public MeshPrimitiveFeatureAccelerator(Mesh mesh) {
    this.vao = new GLVertexData(Mode.LINE_SEGMENTS);
    this.mesh = mesh;
  }

  public void setShowEdges(boolean showEdges) {
    if (showEdges != this.showEdges) {
      this.showEdges = showEdges;
      this.vao.dispose();
    }
  }

  public void setShowEdges(boolean showEdges, M3d edgeColor) {
    this.edgeColor = edgeColor;
    this.showEdges = showEdges;
    this.vao.dispose();
  }

  public boolean getShowEdges() {
    return showEdges;
  }

  public void setShowNormals(boolean showNormals) {
    if (showNormals != this.showNormals) {
      this.showNormals = showNormals;
      this.vao.dispose();
    }
  }

  public boolean getShowNormals() {
    return showNormals;
  }

  public void render(GLCanvas glCanvas) {
    if (!vao.isCompiled()) {
      for (Face face : mesh) {
        if (showEdges) {
          for (int i = 0; i <= face.size(); i++) {
            Vertex A = face.getVertex(i);
            Vertex B = face.getVertex(i + 1);
            Edge e = new Edge(A, B);
            vao.color(e.isBoundaryEdge() ? RED : edgeColor);
            vao.vertex(A);
            vao.vertex(B);
          }
        }
        
        if (showNormals) {
          double dist = 0;
          for (int i = 0; i <= face.size(); i++) {
            dist = Math.max(dist, face.getVertex(i).minus(face.getVertex(i + 1)).length());
          }
          vao.color(face.getNormal());
          vao.vertex(face.getCenter().plus(face.getNormal().times(0.0001)));
          vao.vertex(face.getCenter().plus(face.getNormal().times(dist / 2)));
        }
      }
    }
    vao.render(glCanvas);
  }
  
  public void dispose() {
    vao.dispose();
  }
}
