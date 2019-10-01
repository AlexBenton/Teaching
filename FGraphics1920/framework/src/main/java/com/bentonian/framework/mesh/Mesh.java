package com.bentonian.framework.mesh;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Vec3;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Mesh extends LinkedList<MeshFace> {

  public Mesh() {
  }

  public Map<MeshVertex, MeshVertex> copy(Mesh mesh) {
    Map<MeshVertex, MeshVertex> newVerts = mesh.copyVertices();

    for (MeshFace face : mesh) {
      MeshVertex[] arr = new MeshVertex[face.size()];
      for (int i = 0; i < face.size(); i++) {
        arr[i] = newVerts.get(face.get(i));
      }
      add(new MeshFace(arr));
    }
    computeAllNormals();
    return newVerts;
  }

  public Set<MeshVertex> getVertices() {
    Set<MeshVertex> vertices = Sets.newHashSet();

    for (MeshFace face : this) {
      for (MeshVertex v : face) {
        vertices.add(v);
      }
    }
    return vertices;
  }

  public void computeAllNormals() {
    for (MeshFace face : this) {
      face.computeNormal();
    }
    for (MeshVertex v : getVertices()) {
      v.computeNormal();
    }
  }

  public void scale(Vec3 scale) {
    Set<MeshVertex> vertices = getVertices();
    for (MeshVertex v : vertices) {
      v.set(new Vec3(
          v.getX() * scale.getX(),
          v.getY() * scale.getY(),
          v.getZ() * scale.getZ()));
    }
    for (MeshVertex v : vertices) {
      v.computeNormal();
    }
  }

  public void flip() {
    Mesh newMesh = new Mesh();
    Map<MeshVertex, MeshVertex> newVerts = copyVertices();

    for (MeshFace face : this) {
      int n = face.size();
      MeshVertex arr[] = new MeshVertex[n];

      for (int i = 0; i < n; i++) {
        arr[n-1-i] = newVerts.get(face.get(i));
      }
      newMesh.add(new MeshFace(arr));
    }
    setValue(newMesh);
  }

  public Mesh centerAtOrigin() {
    Vec3 pt = new Vec3();
    Set<MeshVertex> vertices = getVertices();
    for (MeshVertex v : vertices) {
      pt = pt.plus(v);
    }
    pt = pt.times(1.0 / vertices.size());
    for (Vec3 v : vertices) {
      v.setX(v.getX() - pt.getX());
      v.setY(v.getY() - pt.getY());
      v.setZ(v.getZ() - pt.getZ());
    }
    return this;
  }

  public void unitRadius() {
    Set<MeshVertex> vertices = getVertices();
    Vec3 least = new Vec3();
    Vec3 most = new Vec3();
    getBounds(least, most);
    double dx = most.getX() - least.getX();
    double dy = most.getY() - least.getY();
    double dz = most.getZ() - least.getZ();
    double d = 1.0 / max(dx, max(dy, dz));
    Vec3 shift = new Vec3(dx / 2, dy / 2, dz / 2);
    for (Vec3 v : vertices) {
      v.set(v.minus(least).minus(shift).times(d));
    }
  }

  public boolean isTriangulated() {
    for (MeshFace face : this) {
      if (face.size() > 3) {
        return false;
      }
    }
    return true;
  }

  public void triangulate() {
    Mesh newMesh = new Mesh();
    Map<MeshVertex, MeshVertex> newVerts = copyVertices();
    for (MeshFace face : this) {
      MeshVertex A = new MeshVertex(face.getCenter());
      for (int i = 0; i < face.size(); i++) {
        MeshVertex B = newVerts.get(face.get(i));
        MeshVertex C = newVerts.get(face.get(i + 1));
        newMesh.add(new MeshFace(A, B, C));
      }
    }
    setValue(newMesh);
  }

  public boolean isQuads() {
    for (MeshFace face : this) {
      if (face.size() != 4) {
        return false;
      }
    }
    return true;
  }

  public void quadrangulate() {
    Mesh newMesh = new Mesh();
    Map<MeshVertex, MeshVertex> newVerts = copyVertices();
    Map<MeshEdge, MeshVertex> edgeMidpoints = Maps.newHashMap();

    for (MeshFace face : this) {
      for (int i = 0; i < face.size(); i++) {
        MeshEdge e = new MeshEdge(face.get(i), face.get(i + 1));
        if (!edgeMidpoints.containsKey(e)) {
          edgeMidpoints.put(e, new MeshVertex(e.getMidpoint()));
        }
      }
    }

    for (MeshFace face : this) {
      MeshVertex A = new MeshVertex(face.getCenter());
      for (int i = 0; i < face.size(); i++) {
        MeshVertex C = newVerts.get(face.get(i));
        MeshVertex B = edgeMidpoints.get(new MeshEdge(C, face.get(i - 1)));
        MeshVertex D = edgeMidpoints.get(new MeshEdge(C, face.get(i + 1)));
        newMesh.add(new MeshFace(A, B, C, D));
      }
    }
    setValue(newMesh);
  }

  public void getBounds(Vec3 min, Vec3 max) {
    min.set(get(0).get(0));
    max.set(get(0).get(0));
    for (MeshVertex v : getVertices()) {
      min.set(min(min.getX(), v.getX()),
          min(min.getY(), v.getY()),
          min(min.getZ(), v.getZ()));
      max.set(max(max.getX(), v.getX()),
          max(max.getY(), v.getY()),
          max(max.getZ(), v.getZ()));
    }
  }

  public Map<MeshVertex, MeshVertex> copyVertices() {
    Map<MeshVertex, MeshVertex> newVerts = Maps.newHashMap();
    for (MeshFace face : this) {
      for (MeshVertex v : face) {
        if (!newVerts.containsKey(v)) {
          newVerts.put(v, new MeshVertex(v));
        }
      }
    }
    return newVerts;
  }
  
  public void checkMesh() {
    for (MeshFace face : this) {
      
      // Check face size
      if (face.size() < 3) {
        throw new IllegalArgumentException("Face found with only " + face.size() + " vertices");
      }
      
      // Check face convexity
      for (int i = 0; i < face.size(); i++) {
        Vec3 a = face.get(i + 0);
        Vec3 b = face.get(i + 1);
        Vec3 c = face.get(i + 2);
        if (c.minus(b).cross(a.minus(b)).dot(face.getNormal()) < 0) {
          throw new IllegalArgumentException("Non-convex face found");
        }
      }
      
      // Check face normal
      if (face.getNormal().length() < MathConstants.EPSILON) {
        throw new IllegalArgumentException("Face found with bad normal");
      }
      
      // Check face bindings to vertices
      for (MeshVertex v : face) {
        for (MeshFace f : v.getFaces()) {
          if (!contains(f)) {
            throw new IllegalArgumentException("Face found on vertex that's not in this mesh");
          }
        }
      }
      
      // Check vertex bindings to faces
      for (MeshVertex v : face) {
        if (v.getFaceIndex(face) == -1) {
          throw new IllegalArgumentException("Vertex found on face that's unaware of the face");
        }
      }
      
      // Check edges
      for (int i = 0; i <= face.size(); i++) {
        MeshVertex A = face.get(i);
        MeshVertex B = face.get(i + 1);
        MeshEdge e = new MeshEdge(A, B);

        if (e.getFaces().isEmpty()) {
          throw new IllegalArgumentException("Edge found with no faces attached");
        }
      }
    }
  }

  private void setValue(Mesh mesh) {
    clear();
    addAll(mesh);
    computeAllNormals();
  }
}
