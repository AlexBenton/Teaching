package com.bentonian.framework.mesh;

import static java.lang.Math.acos;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Arrays;

import com.bentonian.framework.math.M3d;

public class Face extends ArrayList<Vertex> {

  private static int hashSeed = 0;
  
  private M3d normal;
  private int hash;

  public Face(Vertex... verts) {
    this.hash = hashSeed++;
    addAll(Arrays.asList(verts));
    for (int i = 0; i <size(); i++) {
      verts[i].addFace(this, i);
    }
    computeNormal();
  }

  public void computeNormal() {
    this.normal = getVertex(1).minus(get(0))
        .cross(get(size() - 1).minus(get(0))).normalized();
  }

  public M3d getNormal() {
    return normal;
  }

  public Vertex getVertex(int i) {
    while (i < 0) {
      i += size();
    }
    return get(i % size());
  }

  public double getFaceAngle(Vertex v) {
    int i = v.getFaceIndex(this);
    return acos(getVertex(i-1).minus(v).normalized().dot(getVertex(i+1).minus(v).normalized()));
  }

  public Vertex findOther(Vertex A, Vertex B) {
    for (Vertex C : this) {
      if (!C.equals(A) && !C.equals(B)) {
        return C;
      }
    }
    return null;
  }

  public void getBounds(M3d min, M3d max) {
    min.set(get(0));
    max.set(get(0));
    for (Vertex v : this) {
      min.set(min(min.getX(), v.getX()),
          min(min.getY(), v.getY()), 
          min(min.getZ(), v.getZ()));
      max.set(max(max.getX(), v.getX()),
          max(max.getY(), v.getY()), 
          max(max.getZ(), v.getZ()));
    }
  }
  
  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Face) ? ((Face) obj).hash == this.hash : false;
  }
}