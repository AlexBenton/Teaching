package com.bentonian.framework.mesh;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.bentonian.framework.math.M3d;
import com.google.common.collect.Lists;

public class MeshUtil {

  public static Mesh union(Mesh A, Mesh B) {
    List<MeshFace> facesToCopyFromA = Lists.newLinkedList();
    List<MeshFace> facesToCopyFromB = Lists.newLinkedList();
    Map<MeshVertex, MeshVertex> newVertsA = A.copyVertices();
    Map<MeshVertex, MeshVertex> newVertsB = B.copyVertices();
    Mesh result = new Mesh();

    facesToCopyFromA.addAll(A);
    for (MeshFace b : B) {
      addToBOrRemoveFromA(facesToCopyFromA, facesToCopyFromB, b);
    }

    for (MeshFace a : facesToCopyFromA) {
      MeshVertex[] newFace = new MeshVertex[a.size()];
      for (int i = 0; i < a.size(); i++) {
        newFace[i] = newVertsA.get(a.get(i));
      }
      result.add(new MeshFace(newFace));
    }

    for (MeshFace b : facesToCopyFromB) {
      MeshVertex[] newFace = new MeshVertex[b.size()];
      for (int i = 0; i < b.size(); i++) {
        newFace[i] = newVertsA.get(b.get(i));
        if (newFace[i] == null) {
          newFace[i] = newVertsB.get(b.get(i));
        }
      }
      result.add(new MeshFace(newFace));
    }

    result.computeAllNormals();
    return result;
  }

  public static Mesh difference(Mesh A, Mesh B) {
    List<MeshFace> facesToCopyFromA = Lists.newLinkedList();
    List<MeshFace> facesToCopyFromB = Lists.newLinkedList();
    Map<MeshVertex, MeshVertex> newVertsA = A.copyVertices();
    Mesh result = new Mesh();

    facesToCopyFromA.addAll(A);
    for (MeshFace b : B) {
      addToBOrRemoveFromA(facesToCopyFromA, facesToCopyFromB, b);
    }

    for (MeshFace a : facesToCopyFromA) {
      MeshVertex[] newFace = new MeshVertex[a.size()];
      for (int i = 0; i < a.size(); i++) {
        newFace[i] = newVertsA.get(a.get(i));
      }
      result.add(new MeshFace(newFace));
    }

    result.computeAllNormals();
    return result;
  }

  public static Mesh translate(Mesh mesh, M3d d) {
    Map<MeshVertex, MeshVertex> newVerts = mesh.copyVertices();
    Mesh result = new Mesh();

    for (MeshVertex v : newVerts.values()) {
      v.set(v.plus(d));
    }
    for (MeshFace face : mesh) {
      MeshVertex[] newFace = new MeshVertex[face.size()];
      for (int i = 0; i < face.size(); i++) {
        newFace[i] = newVerts.get(face.get(i));
      }
      result.add(new MeshFace(newFace));
    }

    result.computeAllNormals();
    return result;
  }

  /////////////////////////////////////////////////////////////////////////////

  private static void addToBOrRemoveFromA(List<MeshFace> A, List<MeshFace> B, MeshFace face) {
    Iterator<MeshFace> iter = A.iterator();
    while (iter.hasNext()) {
      if (iter.next().matches(face)) {
        iter.remove();
        return;
      }
    }
    B.add(face);
  }
}
