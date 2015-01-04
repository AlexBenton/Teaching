package com.bentonian.framework.mesh;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Mesh extends LinkedList<Face> {

  public Mesh() {
  }

  public Map<Vertex, Vertex> copy(Mesh mesh) {
    Map<Vertex, Vertex> newVerts = mesh.copyVertices();

    for (Face face : mesh) {
      Vertex[] arr = new Vertex[face.size()];
      for (int i = 0; i < face.size(); i++) {
        arr[i] = newVerts.get(face.get(i));
      }
      add(new Face(arr));
    }
    computeAllNormals();
    return newVerts;
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

  public Mesh centerAtOrigin() {
    M3d pt = new M3d();
    Set<Vertex> vertices = getVertices();
    for (Vertex v : vertices) {
      pt = pt.plus(v);
    }
    pt = pt.times(1.0 / vertices.size());
    for (M3d v : vertices) {
      v.setX(v.getX() - pt.getX());
      v.setY(v.getY() - pt.getY());
      v.setZ(v.getZ() - pt.getZ());
    }
    return this;
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
      Vertex A = new Vertex(face.getCenter());
      for (int i = 0; i < face.size(); i++) {
        Vertex B = newVerts.get(face.getVertex(i));
        Vertex C = newVerts.get(face.getVertex(i + 1));
        newMesh.add(new Face(A, B, C));
      }
    }
    setValue(newMesh);
  }

  public boolean isQuads() {
    for (Face face : this) {
      if (face.size() != 4) {
        return false;
      }
    }
    return true;
  }

  public void quadrangulate() {
    Mesh newMesh = new Mesh();
    Map<Vertex, Vertex> newVerts = copyVertices();
    Map<Edge, Vertex> edgeMidpoints = Maps.newHashMap();

    for (Face face : this) {
      for (int i = 0; i < face.size(); i++) {
        Edge e = new Edge(face.getVertex(i), face.getVertex(i + 1));
        if (!edgeMidpoints.containsKey(e)) {
          edgeMidpoints.put(e, new Vertex(e.getMidpoint()));
        }
      }
    }

    for (Face face : this) {
      Vertex A = new Vertex(face.getCenter());
      for (int i = 0; i < face.size(); i++) {
        Vertex C = newVerts.get(face.getVertex(i));
        Vertex B = edgeMidpoints.get(new Edge(C, face.getVertex(i - 1)));
        Vertex D = edgeMidpoints.get(new Edge(C, face.getVertex(i + 1)));
        newMesh.add(new Face(A, B, C, D));
      }
    }
    setValue(newMesh);
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

  public Map<Vertex, Vertex> copyVertices() {
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

  private void setValue(Mesh mesh) {
    clear();
    addAll(mesh);
    computeAllNormals();
  }
}
