package com.bentonian.framework.mesh.subdivision;

import java.util.Map;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Edge;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.google.common.collect.Maps;

public class CatmullClark implements SubdivisionFunction {

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
      Vertex newFaceVertex = faceRule(face);

      for (int i = 0; i < face.size(); i++) {
        Vertex prev = face.getVertex(i - 1);
        Vertex curr = face.getVertex(i);
        Vertex next = face.getVertex(i + 1);

        newMesh.add(new Face(
            edgeNextGen.get(new Edge(curr, prev)),
            vertexNextGen.get(curr),
            edgeNextGen.get(new Edge(curr, next)),
            newFaceVertex));
      }
    }

    newMesh.computeAllNormals();
    return newMesh;
  }

  protected Vertex vertexRule(Vertex v) {
    Edge[] boundary = v.checkForBoundary();

    if (boundary == null) {
      int n = v.getOneRing().size();

      // Average of surrounding faces
      M3d Q = new M3d();
      for (Face neighbor : v.getFaces()) {
        Q = Q.plus(neighbor.getCenter());
      }
      Q = Q.times(1.0 / v.getFaces().size());

      // Average of midpoints of adjacent edges
      M3d R = new M3d();
      for (Vertex neighbor : v.getOneRing()) {
        R = R.plus(v.plus(neighbor).times(0.5));
      }
      R = R.times(1.0 / v.getOneRing().size());

      return new Vertex(v.times(n - 3).plus(Q).plus(R.times(2)).times(1.0 / n));
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
      return new Vertex(e.getA().times(24)
          .plus(e.getB().times(24))
          .plus(alpha.getAverageVerticesExcluding(e.getA(), e.getB()).times(8))
          .plus(omega.getAverageVerticesExcluding(e.getA(), e.getB()).times(8))
          .times(1.0 / 64.0));
    } else {
      return new Vertex(e.getMidpoint());
    }
  }

  protected Vertex faceRule(Face face) {
    return new Vertex(face.getCenter());
  }
}
