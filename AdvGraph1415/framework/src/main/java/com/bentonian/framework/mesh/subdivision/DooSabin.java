package com.bentonian.framework.mesh.subdivision;

import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Edge;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DooSabin implements SubdivisionFunction {

  @Override
  public Mesh apply(Mesh mesh) {
    Map<Face, Map<Vertex, Vertex>> nextGen = Maps.newHashMap();
    Map<Edge, Map<Vertex, Vertex>> boundaryNextGen = Maps.newHashMap();
    Set<Edge> edges = Sets.newHashSet();
    Mesh newMesh = new Mesh();

    for (Face face : mesh) {
      Map<Vertex, Vertex> nextGenVerts = Maps.newHashMap();
      Vertex[] innerFaceNextGen = new Vertex[face.size()];
      int i = 0;

      nextGen.put(face, nextGenVerts);
      for (Vertex v : face) {
        Vertex nextGenVertex = vertexRule(face, v);
        nextGenVerts.put(v,  nextGenVertex);
        innerFaceNextGen[i++] = nextGenVertex;
      }
      newMesh.add(new Face(innerFaceNextGen));
    }

    for (Face face : mesh) {
      for (Vertex v : face) {
        Edge e = new Edge(v, face.getVertex(v.getFaceIndex(face) + 1));
        if (e.isBoundaryEdge()) {
          boundaryNextGen.put(e, ImmutableMap.of(
              e.getA(), boundaryRule(e.getA(), e.getB()),
              e.getB(), boundaryRule(e.getB(), e.getA())));
        }
      }
    }

    for (Face face : mesh) {
      Map<Vertex, Vertex> myNextGen = nextGen.get(face);
      for (int i = 0; i < face.size(); i++) {
        Vertex A = face.getVertex(i + 1);
        Vertex B = face.getVertex(i);
        Vertex C, D;
        Edge e = new Edge(A, B);

        if (!edges.contains(e)) {
          Face neighbor = e.getOtherFace(face);
          if (neighbor != null) {
            int neighborIndex = B.getFaceIndex(neighbor);
            Map<Vertex, Vertex> neighborNextGen = nextGen.get(neighbor);
            C = neighborNextGen.get(neighbor.getVertex(neighborIndex));
            D = neighborNextGen.get(neighbor.getVertex(neighborIndex - 1));
          } else {
            Map<Vertex, Vertex> boundary = boundaryNextGen.get(e);
            C = boundary.get(B);
            D = boundary.get(A);
          }
          newMesh.add(new Face(myNextGen.get(A), myNextGen.get(B), C, D));
          edges.add(e);
        }
      }
    }

    for (Vertex v : mesh.getVertices()) {
      Face[] faceRing = v.getFaceRingOrdered();
      Edge[] boundary = v.checkForBoundary();
      Vertex[] vertexNextGen = new Vertex[faceRing.length + ((boundary == null) ? 0 : 2)];
      int j = 0;
      
      if (boundary != null) {
        Face first = faceRing[0];
        Edge e = new Edge(v, first.getVertex(v.getFaceIndex(first) + 1));
        Map<Vertex, Vertex> edgeNextGen = boundaryNextGen.get(e);
        vertexNextGen[j++] = edgeNextGen.get(v);
      }
      for (int i = 0; i < faceRing.length; i++) {
        vertexNextGen[j++] = nextGen.get(faceRing[i]).get(v);
      }
      if (boundary != null) {
        Face last = faceRing[faceRing.length - 1];
        Edge e = new Edge(v, last.getVertex(v.getFaceIndex(last) - 1));
        Map<Vertex, Vertex> edgeNextGen = boundaryNextGen.get(e);
        vertexNextGen[j++] = edgeNextGen.get(v);
      }
      newMesh.add(new Face(vertexNextGen));
    }

    return newMesh;
  }

  protected Vertex vertexRule(Face face, Vertex vertex) {
    int x = vertex.getFaceIndex(face);
    int k = face.size();
    if (k == 4) {
      return new Vertex(vertex.times(9)
          .plus(face.getVertex(x - 1).times(3))
          .plus(face.getVertex(x + 1).times(3))
          .plus(face.getVertex(x + 2).times(1))
          .times(1.0 / 16.0));
    } else {
      M3d pt = vertex.times(0.25 + 5.0 / (4.0 * k));
      for (int i = 1; i < k; i++) {
        pt = pt.plus(face.getVertex(x + i).times((3 + 2 * Math.cos(2 * i * Math.PI / k)) / (4 * k)));
      }
      return new Vertex(pt);
    }
  }

  protected Vertex boundaryRule(Vertex near, Vertex far) {
    return new Vertex(near.times(0.75).plus(far.times(0.25)));
  }
}
