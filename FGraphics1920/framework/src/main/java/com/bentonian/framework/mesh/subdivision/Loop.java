package com.bentonian.framework.mesh.subdivision;

import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.MeshEdge;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshVertex;
import com.google.common.collect.Maps;


public class Loop implements SubdivisionFunction {

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
        MeshEdge e = new MeshEdge(face.get(i), face.get(i + 1));
        if (!edgeNextGen.containsKey(e)) {
          edgeNextGen.put(e, edgeRule(e));
        }
      }
    }

    for (MeshFace face : mesh) {
      MeshVertex[] innerFace = new MeshVertex[face.size()];

      for (int i = 0; i < face.size(); i++) {
        MeshVertex prev = face.get(i - 1);
        MeshVertex curr = face.get(i);
        MeshVertex next = face.get(i + 1);
        MeshVertex prevEdge = edgeNextGen.get(new MeshEdge(curr, prev));
        MeshVertex nextEdge = edgeNextGen.get(new MeshEdge(curr, next));

        newMesh.add(new MeshFace(prevEdge, vertexNextGen.get(curr), nextEdge));
        innerFace[i] = nextEdge;
      }
      MeshFace newFace = new MeshFace(innerFace);
      newMesh.add(newFace);

      for (int i = 0; i < newFace.size(); i++) {
        MeshVertex A = newFace.get(i);
        MeshVertex B = newFace.get(i + 1);
        MeshEdge e = new MeshEdge(A, B);
        MeshFace neighbor = e.getOtherFace(newFace);
        if (neighbor == null) {
          throw new RuntimeException("Missing neighbor after vertex face generation");
        } else if (A.getFaceIndex(neighbor) + 1 == B.getFaceIndex(neighbor)) {
          throw new RuntimeException("Flipped faces!!!");
        }
      }
    }

    newMesh.computeAllNormals();
    return newMesh;
  }

  protected MeshVertex vertexRule(MeshVertex v) {
    MeshEdge[] boundary = v.checkForBoundary();

    if (boundary == null) {
      Set<MeshVertex> oneRing = v.getOneRing();
      int k = oneRing.size();
      double beta = 3.0 / ((k < 4) ? 16.0 : (8 * k));
      Vec3 pt = new Vec3(v.times(1 - k * beta));

      for (MeshVertex neighbor : oneRing) {
        pt = pt.plus(neighbor.times(beta));
      }
      return new MeshVertex(pt);
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
      return new MeshVertex(e.getA().times(3)
          .plus(e.getB().times(3))
          .plus(alpha.getAverageVerticesExcluding(e.getA(), e.getB()))
          .plus(omega.getAverageVerticesExcluding(e.getA(), e.getB()))
          .times(1.0 / 8.0));
    } else {
      return new MeshVertex(e.getMidpoint());
    }
  }
}
