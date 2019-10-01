package com.bentonian.framework.mesh.subdivision;

import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.MeshEdge;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshVertex;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DooSabin implements SubdivisionFunction {

  @Override
  public Mesh apply(Mesh mesh) {
    Map<MeshFace, Map<MeshVertex, MeshVertex>> nextGen = Maps.newHashMap();
    Map<MeshEdge, Map<MeshVertex, MeshVertex>> boundaryNextGen = Maps.newHashMap();
    Set<MeshEdge> edges = Sets.newHashSet();
    Mesh newMesh = new Mesh();

    for (MeshFace face : mesh) {
      Map<MeshVertex, MeshVertex> nextGenVerts = Maps.newHashMap();
      MeshVertex[] innerFaceNextGen = new MeshVertex[face.size()];
      int i = 0;

      nextGen.put(face, nextGenVerts);
      for (MeshVertex v : face) {
        MeshVertex nextGenVertex = vertexRule(face, v);
        nextGenVerts.put(v,  nextGenVertex);
        innerFaceNextGen[i++] = nextGenVertex;
      }
      newMesh.add(new MeshFace(innerFaceNextGen));
    }

    for (MeshFace face : mesh) {
      for (MeshVertex v : face) {
        MeshEdge e = new MeshEdge(v, face.get(v.getFaceIndex(face) + 1));
        if (e.isBoundaryEdge()) {
          boundaryNextGen.put(e, ImmutableMap.of(
              e.getA(), boundaryRule(e.getA(), e.getB()),
              e.getB(), boundaryRule(e.getB(), e.getA())));
        }
      }
    }

    for (MeshFace face : mesh) {
      Map<MeshVertex, MeshVertex> myNextGen = nextGen.get(face);
      for (int i = 0; i < face.size(); i++) {
        MeshVertex A = face.get(i + 1);
        MeshVertex B = face.get(i);
        MeshVertex C, D;
        MeshEdge e = new MeshEdge(A, B);

        if (!edges.contains(e)) {
          MeshFace neighbor = e.getOtherFace(face);
          if (neighbor != null) {
            int neighborIndex = B.getFaceIndex(neighbor);
            Map<MeshVertex, MeshVertex> neighborNextGen = nextGen.get(neighbor);
            C = neighborNextGen.get(neighbor.get(neighborIndex));
            D = neighborNextGen.get(neighbor.get(neighborIndex - 1));
          } else {
            Map<MeshVertex, MeshVertex> boundary = boundaryNextGen.get(e);
            C = boundary.get(B);
            D = boundary.get(A);
          }
          newMesh.add(new MeshFace(myNextGen.get(A), myNextGen.get(B), C, D));
          edges.add(e);
        }
      }
    }

    for (MeshVertex v : mesh.getVertices()) {
      MeshFace[] faceRing = v.getFaceRingOrdered();
      MeshEdge[] boundary = v.checkForBoundary();
      MeshVertex[] vertexNextGen = new MeshVertex[faceRing.length + ((boundary == null) ? 0 : 2)];
      int j = 0;
      
      if (boundary != null) {
        MeshFace first = faceRing[0];
        MeshEdge e = new MeshEdge(v, first.get(v.getFaceIndex(first) + 1));
        Map<MeshVertex, MeshVertex> edgeNextGen = boundaryNextGen.get(e);
        vertexNextGen[j++] = edgeNextGen.get(v);
      }
      for (int i = 0; i < faceRing.length; i++) {
        vertexNextGen[j++] = nextGen.get(faceRing[i]).get(v);
      }
      if (boundary != null) {
        MeshFace last = faceRing[faceRing.length - 1];
        MeshEdge e = new MeshEdge(v, last.get(v.getFaceIndex(last) - 1));
        Map<MeshVertex, MeshVertex> edgeNextGen = boundaryNextGen.get(e);
        vertexNextGen[j++] = edgeNextGen.get(v);
      }
      newMesh.add(new MeshFace(vertexNextGen));
    }

    return newMesh;
  }

  protected MeshVertex vertexRule(MeshFace face, MeshVertex vertex) {
    int x = vertex.getFaceIndex(face);
    int k = face.size();
    if (k == 4) {
      return new MeshVertex(vertex.times(9)
          .plus(face.get(x - 1).times(3))
          .plus(face.get(x + 1).times(3))
          .plus(face.get(x + 2).times(1))
          .times(1.0 / 16.0));
    } else {
      Vec3 pt = vertex.times(0.25 + 5.0 / (4.0 * k));
      for (int i = 1; i < k; i++) {
        pt = pt.plus(face.get(x + i).times((3 + 2 * Math.cos(2 * i * Math.PI / k)) / (4 * k)));
      }
      return new MeshVertex(pt);
    }
  }

  protected MeshVertex boundaryRule(MeshVertex near, MeshVertex far) {
    return new MeshVertex(near.times(0.75).plus(far.times(0.25)));
  }
}
