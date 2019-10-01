package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.material.Colors.BLACK;
import static com.bentonian.framework.material.Colors.RED;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshEdge;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshVertex;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;
import com.bentonian.framework.ui.GLVertexData.Mode;

public class MeshPrimitiveFeatureAccelerator {
  
  private final Mesh mesh;
  private final GLVertexData vao;
  
  private Vec3 edgeColor = BLACK;
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

  public void setShowEdges(boolean showEdges, Vec3 edgeColor) {
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
    glCanvas.pushProgram(GLCanvas.DEFAULT_SHADER_PROGRAM);
    
    if (!vao.isCompiled()) {
      for (MeshFace face : mesh) {
        if (showEdges) {
          for (int i = 0; i <= face.size(); i++) {
            MeshVertex A = face.get(i);
            MeshVertex B = face.get(i + 1);
            MeshEdge e = new MeshEdge(A, B);
            vao.color(e.isBoundaryEdge() ? RED : edgeColor);
            vao.vertex(A);
            vao.vertex(B);
          }
        }
        
        if (showNormals) {
          double dist = 0;
          for (int i = 0; i <= face.size(); i++) {
            dist = Math.max(dist, face.get(i).minus(face.get(i + 1)).length());
          }
          vao.color(face.getNormal());
          vao.vertex(face.getCenter().plus(face.getNormal().times(0.0001)));
          vao.vertex(face.getCenter().plus(face.getNormal().times(dist / 2)));
        }
      }
    }
    vao.render(glCanvas);
    
    glCanvas.popProgram();
  }
  
  public void dispose() {
    vao.dispose();
  }
}
