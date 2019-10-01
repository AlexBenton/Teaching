package com.bentonian.framework.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.ui.Vertex;
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

  public static Mesh translate(Mesh mesh, Vec3 d) {
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

  /**
   * Input is a list of vertices as pairs, so expect every vertex at least twice (often more)
   */
  public static List<Vertex> simplifyToLoop(List<Vertex> verticesInPairs) {
    Map<Edge, Integer> edges = new HashMap<>();
    
    for (int i = 0; i < verticesInPairs.size(); i += 2) {
      inc(edges, new Edge(verticesInPairs.get(i), verticesInPairs.get(i + 1)));
    }
      
    Set<Edge> singleEdges = new HashSet<>();
    for (Entry<Edge, Integer> edge : edges.entrySet()) {
      if (edge.getValue() == 1) {
        singleEdges.add(edge.getKey());
      }
    }
    
    return flatten(singleEdges);
  }
  
  public static void ensureCCW(List<Vertex> vertices, Vec3 normal) {
    Vec3 a = vertices.get(0);
    Vec3 b = vertices.get(1);
    Vec3 c = vertices.get(2);
    
    if (c.minus(b).cross(a.minus(b)).normalized().dot(normal) < 0) {
      Collections.reverse(vertices);
    }
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
  
  private static class Edge {
    Vertex a;
    Vertex b;
    
    Edge(Vertex a, Vertex b) { this.a = a; this.b = b; }
    @Override public int hashCode() { return a.hashCode() + b.hashCode(); }
    @Override public boolean equals(Object o) { return hashCode() == ((Edge) o).hashCode(); }
    boolean linkedTo(Vertex c) { return a.equals(c) || b.equals(c); }
    Vertex other(Vertex c) { return a.equals(c) ? b : a; }
  }
  
  private static void inc(Map<Edge, Integer> map, Edge e) {
    Integer n = map.get(e);
    map.put(e, ((n == null) ? 0 : n) + 1);
  }
  
  private static List<Vertex> flatten(Set<Edge> edges) {
    List<Vertex> result = new ArrayList<>();
    if (edges.isEmpty()) {
      return result;
    }
    
    Edge e = edges.iterator().next();
    edges.remove(e);
    result.add(e.a);
    result.add(e.b);
    
    Vertex v = e.b;
    while (!edges.isEmpty()) {
      boolean foundRemovable = false;
      for (Edge candidate : edges) {
        if (candidate.linkedTo(v)) {
          v = candidate.other(v);
          edges.remove(candidate);
          result.add(v);
          foundRemovable = true;
          break;
        }
      }
      if (!foundRemovable) {
        throw new IllegalArgumentException("Vertex loop is actually two+ loops");
      }
    }
    
    return result;
  }
}
