package com.bentonian.framework.mesh;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Mesh extends LinkedList<Face> {

  public Mesh() {
  }

  public void setValue(Mesh mesh) {
    clear();
    addAll(mesh);
    computeAllNormals();
  }
  
  public Set<Vertex> getVertices() {
    Set<Vertex> vertices = Sets.newHashSet();

    for (Face face : this) {
      for (Vertex v : face) {
        vertices.add(v);
      }
    }
    return vertices;
  }

  public void computeAllNormals() {
    for (Face face : this) {
      face.computeNormal();
    }
    for (Vertex v : getVertices()) {
      v.computeNormal();
    }
  }

  public void scale(M3d scale) {
    Set<Vertex> vertices = getVertices();
    for (Vertex v : vertices) {
      v.set(new M3d(
          v.getX() * scale.getX(),
          v.getY() * scale.getY(),
          v.getZ() * scale.getZ()));
    }
    for (Vertex v : vertices) {
      v.computeNormal();
    }
  }

  public void flip() {
    Mesh newMesh = new Mesh();
    Map<Vertex, Vertex> newVerts = copyVertices();

    for (Face face : this) {
      int n = face.size();
      Vertex arr[] = new Vertex[n];

      for (int i = 0; i < n; i++) {
        arr[n-1-i] = newVerts.get(face.get(i));
      }
      newMesh.add(new Face(arr));
    }
    setValue(newMesh);
  }

  public void centerUnderHighestPoint() {
    M3d highest = get(0).get(0);
    Set<Vertex> vertices = getVertices();
    for (Vertex v : vertices) {
      if (v.getY() > highest.getY()) {
        highest = v;
      }
    }
    highest = new M3d(highest);
    for (M3d v : vertices) {
      v.setX(v.getX() - highest.getX());
      v.setZ(v.getZ() - highest.getZ());
    }
  }

  public void getBounds(M3d min, M3d max) {
    min.set(get(0).get(0));
    max.set(get(0).get(0));
    for (Vertex v : getVertices()) {
      min.set(min(min.getX(), v.getX()),
          min(min.getY(), v.getY()), 
          min(min.getZ(), v.getZ()));
      max.set(max(max.getX(), v.getX()),
          max(max.getY(), v.getY()), 
          max(max.getZ(), v.getZ()));
    }
  }

  public void unitRadius() {
    Set<Vertex> vertices = getVertices();
    M3d least = new M3d();
    M3d most = new M3d();
    getBounds(least, most);
    double dx = most.getX() - least.getX();
    double dy = most.getY() - least.getY();
    double dz = most.getZ() - least.getZ();
    double d = 1.0 / max(dx, max(dy, dz));
    M3d shift = new M3d(dx / 2, dy / 2, dz / 2);
    for (M3d v : vertices) {
      v.set(v.minus(least).minus(shift).times(d));
    }
  }

  public boolean isTriangulated() {
    for (Face face : this) {
      if (face.size() > 3) {
        return false;
      }
    }
    return true;
  }

  public void triangulate() {
    Mesh newMesh = new Mesh();
    Map<Vertex, Vertex> newVerts = copyVertices();
    for (Face face : this) {
      Vertex A = newVerts.get(face.get(0));
      for (int i = 0; i < face.size() - 2; i++) {
        Vertex B = newVerts.get(face.get(i + 1));
        Vertex C = newVerts.get(face.get(i + 2));
        newMesh.add(new Face(A, B, C));
      }
    }
    setValue(newMesh);
  }

  // Crease angle = 15°
  public void smooth() {
    smooth(15 * 2 * PI / 360);
  }

  // Loop subdivision
  public void smooth(double creaseThresholdRadians) {
    if (!isTriangulated()) {
      triangulate();
    }

    Mesh newMesh = new Mesh();
    Map<Vertex, Vertex> newVerts = Maps.newHashMap();
    Map<Edge, Vertex> newEdgeVerts = Maps.newHashMap();

    // Vertex -> Vertex
    for (Face face : this) {
      for (Vertex v : face) {
        if (!newVerts.containsKey(v)) {
          Vertex newVert = new Vertex(v);
          Set<Vertex> oneRing = v.getOneRing();
          List<Vertex> verticesOnTheEdge = v.checkForEdge(oneRing);

          if (verticesOnTheEdge.isEmpty() && (abs(v.getAngleDeficit()) < creaseThresholdRadians)) {
            double k = oneRing.size();
            double w = (1 / k) * (0.625 - Math.pow(0.375 + 0.25 * cos(2 * PI / k), 2));
            M3d shifted = v.times(1 - k * w);
            for (Vertex neighbor : oneRing) {
              shifted = shifted.plus(neighbor.times(w));
            }
            newVert = new Vertex(shifted);
          } else if ((verticesOnTheEdge.size() == 2)
              && acos(verticesOnTheEdge.get(0).minus(v).normalized().dot(
                  verticesOnTheEdge.get(1).minus(v).normalized())) < creaseThresholdRadians) {
            newVert = new Vertex(
                v.times(0.75)
                .plus(verticesOnTheEdge.get(0).times(0.125))
                .plus(verticesOnTheEdge.get(1).times(0.125)));
          }
          newVerts.put(v, newVert);
        }
      }
    }
    
    // Edge -> Vertex
    for (Face face : this) {
      for (Vertex v : face) {
        Set<Vertex> oneRing = v.getOneRing();
        for (Vertex neighbor : oneRing) {
          Edge edge = new Edge(v, neighbor);
          if (!newEdgeVerts.containsKey(edge)) {
            newEdgeVerts.put(edge, edge.getSubdivisionVertex(creaseThresholdRadians));
          }
        }
      }
    }

    // Face -> Faces
    for (Face face : this) {
      Vertex A = newVerts.get(face.get(0));
      Preconditions.checkNotNull(A);
      Vertex B = newVerts.get(face.get(1));
      Preconditions.checkNotNull(B);
      Vertex C = newVerts.get(face.get(2));
      Preconditions.checkNotNull(C);
      Vertex AB = newEdgeVerts.get(new Edge(face.get(0), face.get(1)));
      Preconditions.checkNotNull(AB);
      Vertex BC = newEdgeVerts.get(new Edge(face.get(1), face.get(2)));
      Preconditions.checkNotNull(BC);
      Vertex CA = newEdgeVerts.get(new Edge(face.get(2), face.get(0)));
      Preconditions.checkNotNull(CA);
      newMesh.add(new Face(A, AB, CA));
      newMesh.add(new Face(B, BC, AB));
      newMesh.add(new Face(C, CA, BC));
      newMesh.add(new Face(AB, BC, CA));
    }

    setValue(newMesh);
  }
  
  /////////////////////////////////////////////////////////////////////////////

  private Map<Vertex, Vertex> copyVertices() {
    Map<Vertex, Vertex> newVerts = Maps.newHashMap();
    for (Face face : this) {
      for (Vertex v : face) {
        if (!newVerts.containsKey(v)) {
          newVerts.put(v, new Vertex(v));
        }
      }
    }
    return newVerts;
  }

  private static class Edge {

    private final Vertex A, B;

    public Edge(Vertex A, Vertex B) {
      this.A = A;
      this.B = B;
    }

    public Vertex getSubdivisionVertex(double creaseThresholdRadians) {
      List<Face> shared = ImmutableList.copyOf(Sets.intersection(A.getFaces(), B.getFaces()));
      if ((shared.size() != 2)
          || (acos(shared.get(0).getNormal().dot(shared.get(1).getNormal())) > creaseThresholdRadians)){
        return new Vertex(A.plus(B).times(0.5));
      } else {
        Vertex X = shared.get(0).findOther(A, B);
        Vertex Y = shared.get(1).findOther(A, B);
        return new Vertex(A.plus(B).times(0.375).plus(X.plus(Y).times(0.125)));
      }
    }

    @Override
    public int hashCode() {
      return A.hashCode() ^ B.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Edge) {
        Edge e = (Edge) obj;
        return (A.equals(e.A) && B.equals(e.B)) || (A.equals(e.B) && B.equals(e.A));
      } else { 
        return false;
      }
    }
  }
}
