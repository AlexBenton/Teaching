package com.bentonian.framework.mesh.subdivision;

import java.util.Map;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.MeshEdge;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshVertex;
import com.google.common.collect.Maps;

public class CatmullClark implements SubdivisionFunction {

  @Override
  public Mesh apply(Mesh mesh) {
    Map<MeshVertex, MeshVertex> vertexNextGen = Maps.newHashMap();
    Map<MeshEdge, MeshVertex> edgeNextGen = Maps.newHashMap();
    Mesh newMesh = new Mesh();

    for (MeshVertex v : mesh.getVertices()) {
      vertexNextGen.put(v, vertexRule(v));
    }

    for (MeshFace face : mesh) {
      for (int i = 0; i < face.size(); i++) {
        MeshEdge e = new MeshEdge(face.getVertex(i), face.getVertex(i + 1));
        if (!edgeNextGen.containsKey(e)) {
          edgeNextGen.put(e, edgeRule(e));
        }
      }
    }

    for (MeshFace face : mesh) {
      MeshVertex newFaceVertex = faceRule(face);

      for (int i = 0; i < face.size(); i++) {
        MeshVertex prev = face.getVertex(i - 1);
        MeshVertex curr = face.getVertex(i);
        MeshVertex next = face.getVertex(i + 1);

        newMesh.add(new MeshFace(
            edgeNextGen.get(new MeshEdge(curr, prev)),
            vertexNextGen.get(curr),
            edgeNextGen.get(new MeshEdge(curr, next)),
            newFaceVertex));
      }
    }

    newMesh.computeAllNormals();
    return newMesh;
  }

  protected MeshVertex vertexRule(MeshVertex v) {
    MeshEdge[] boundary = v.checkForBoundary();

    if (boundary == null) {
      int n = v.getOneRing().size();

      // Average of surrounding faces
      M3d Q = new M3d();
      for (MeshFace neighbor : v.getFaces()) {
        Q = Q.plus(neighbor.getCenter());
      }
      Q = Q.times(1.0 / v.getFaces().size());

      // Average of midpoints of adjacent edges
      M3d R = new M3d();
      for (MeshVertex neighbor : v.getOneRing()) {
        R = R.plus(v.plus(neighbor).times(0.5));
      }
      R = R.times(1.0 / v.getOneRing().size());

      return new MeshVertex(v.times(n - 3).plus(Q).plus(R.times(2)).times(1.0 / n));
    } else {
      return new MeshVertex(
          v.times(0.75)
          .plus(boundary[0].getOtherVertex(v).times(0.125))
          .plus(boundary[1].getOtherVertex(v).times(0.125)));
    }
  }

  protected MeshVertex edgeRule(MeshEdge e) {
    MeshFace alpha = e.getFaceAlpha();
    MeshFace omega = e.getFaceOmega();

    if (omega != null) {
      return new MeshVertex(e.getA().times(24)
          .plus(e.getB().times(24))
          .plus(alpha.getAverageVerticesExcluding(e.getA(), e.getB()).times(8))
          .plus(omega.getAverageVerticesExcluding(e.getA(), e.getB()).times(8))
          .times(1.0 / 64.0));
    } else {
      return new MeshVertex(e.getMidpoint());
    }
  }

  protected MeshVertex faceRule(MeshFace face) {
    return new MeshVertex(face.getCenter());
  }
}
