package com.bentonian.framework.mesh;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bentonian.framework.math.M3d;
import com.google.common.collect.Lists;

public class MeshUtil {

  public static Mesh union(Mesh A, Mesh B) {
    List<Face> facesToCopyFromA = Lists.newLinkedList();
    List<Face> facesToCopyFromB = Lists.newLinkedList();
    Map<Vertex, Vertex> newVertsA = A.copyVertices();
    Map<Vertex, Vertex> newVertsB = B.copyVertices();
    Mesh result = new Mesh();

    facesToCopyFromA.addAll(A);
    for (Face b : B) {
      addToBOrRemoveFromA(facesToCopyFromA, facesToCopyFromB, b);
    }

    for (Face a : facesToCopyFromA) {
      Vertex[] newFace = new Vertex[a.size()];
      for (int i = 0; i < a.size(); i++) {
        newFace[i] = newVertsA.get(a.get(i));
      }
      result.add(new Face(newFace));
    }

    for (Face b : facesToCopyFromB) {
      Vertex[] newFace = new Vertex[b.size()];
      for (int i = 0; i < b.size(); i++) {
        newFace[i] = newVertsA.get(b.get(i));
        if (newFace[i] == null) {
          newFace[i] = newVertsB.get(b.get(i));
        }
      }
      result.add(new Face(newFace));
    }

    result.computeAllNormals();
    return result;
  }

  public static Mesh difference(Mesh A, Mesh B) {
    List<Face> facesToCopyFromA = Lists.newLinkedList();
    List<Face> facesToCopyFromB = Lists.newLinkedList();
    Map<Vertex, Vertex> newVertsA = A.copyVertices();
    Mesh result = new Mesh();

    facesToCopyFromA.addAll(A);
    for (Face b : B) {
      addToBOrRemoveFromA(facesToCopyFromA, facesToCopyFromB, b);
    }

    for (Face a : facesToCopyFromA) {
      Vertex[] newFace = new Vertex[a.size()];
      for (int i = 0; i < a.size(); i++) {
        newFace[i] = newVertsA.get(a.get(i));
      }
      result.add(new Face(newFace));
    }

    result.computeAllNormals();
    return result;
  }

  public static Mesh translate(Mesh mesh, M3d d) {
    Map<Vertex, Vertex> newVerts = mesh.copyVertices();
    Mesh result = new Mesh();

    for (Vertex v : newVerts.values()) {
      v.set(v.plus(d));
    }
    for (Face face : mesh) {
      Vertex[] newFace = new Vertex[face.size()];
      for (int i = 0; i < face.size(); i++) {
        newFace[i] = newVerts.get(face.get(i));
      }
      result.add(new Face(newFace));
    }

    result.computeAllNormals();
    return result;
  }

  /////////////////////////////////////////////////////////////////////////////

  private static void addToBOrRemoveFromA(List<Face> A, List<Face> B, Face face) {
    Iterator<Face> iter = A.iterator();
    while (iter.hasNext()) {
      if (iter.next().matches(face)) {
        iter.remove();
        return;
      }
    }
    B.add(face);
  }
}
