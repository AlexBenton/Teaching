package com.bentonian.framework.mesh.subdivision;

import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Edge;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.google.common.collect.Maps;


public class Loop implements SubdivisionFunction {

  @Override
  public Mesh apply(Mesh mesh) {
    Map<Vertex, Vertex> vertexNextGen = Maps.newHashMap();
    Map<Edge, Vertex> edgeNextGen = Maps.newHashMap();
    Mesh newMesh = new Mesh();

    for (Vertex v : mesh.getVertices()) {
      vertexNextGen.put(v, vertexRule(v));
    }

    for (Face face : mesh) {
      for (int i = 0; i < face.size(); i++) {
        Edge e = new Edge(face.getVertex(i), face.getVertex(i + 1));
        if (!edgeNextGen.containsKey(e)) {
          edgeNextGen.put(e, edgeRule(e));
        }
      }
    }

    for (Face face : mesh) {
      Vertex[] innerFace = new Vertex[face.size()];

      for (int i = 0; i < face.size(); i++) {
        Vertex prev = face.getVertex(i - 1);
        Vertex curr = face.getVertex(i);
        Vertex next = face.getVertex(i + 1);
        Vertex prevEdge = edgeNextGen.get(new Edge(curr, prev));
        Vertex nextEdge = edgeNextGen.get(new Edge(curr, next));

        newMesh.add(new Face(prevEdge, vertexNextGen.get(curr), nextEdge));
        innerFace[i] = nextEdge;
      }
      Face newFace = new Face(innerFace);
      newMesh.add(newFace);

      for (int i = 0; i < newFace.size(); i++) {
        Vertex A = newFace.getVertex(i);
        Vertex B = newFace.getVertex(i + 1);
        Edge e = new Edge(A, B);
        Face neighbor = e.getOtherFace(newFace);
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

  protected Vertex vertexRule(Vertex v) {
    Edge[] boundary = v.checkForBoundary();

    if (boundary == null) {
      Set<Vertex> oneRing = v.getOneRing();
      int k = oneRing.size();
      double beta = 3.0 / ((k < 4) ? 16.0 : (8 * k));
      M3d pt = new M3d(v.times(1 - k * beta));

      for (Vertex neighbor : oneRing) {
        pt = pt.plus(neighbor.times(beta));
      }
      return new Vertex(pt);
    } else {
      return new Vertex(
          v.times(0.75)
          .plus(boundary[0].getOtherVertex(v).times(0.125))
          .plus(boundary[1].getOtherVertex(v).times(0.125)));
    }
  }

  protected Vertex edgeRule(Edge e) {
    Face alpha = e.getFaceAlpha();
    Face omega = e.getFaceOmega();

    if (omega != null) {
      return new Vertex(e.getA().times(3)
          .plus(e.getB().times(3))
          .plus(alpha.getAverageVerticesExcluding(e.getA(), e.getB()))
          .plus(omega.getAverageVerticesExcluding(e.getA(), e.getB()))
          .times(1.0 / 8.0));
    } else {
      return new Vertex(e.getMidpoint());
    }
  }
}
