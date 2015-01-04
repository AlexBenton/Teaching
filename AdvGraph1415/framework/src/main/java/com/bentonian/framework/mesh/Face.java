package com.bentonian.framework.mesh;

import static java.lang.Math.acos;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;

import com.bentonian.framework.math.M3d;

public class Face extends ArrayList<Vertex> {

  private static int hashSeed = 0;

  private final int hash;
  
  private M3d normal;

  public Face(M3d... verts) {
    this(m3dToVertex(verts));
  }

  public Face(Vertex... verts) {
    this.hash = hashSeed++;
    for (Vertex vert : verts) {
      if (vert == null) {
        throw new NullPointerException("Null Vertex received");
      }
      if (!contains(vert)) {
        add(vert);
      }
    }
    if (size() < 3) {
      String s = "after trying to add\n";
      for (Vertex v : verts) {
        s += "  [" + v + "]\n";
      }
      s += "only had " + size() + " vertices in face.";
      throw new RuntimeException("Incomplete face constructed: " + s);
    }
    for (int i = 0; i < size(); i++) {
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
  
  public double getArea() {
    M3d C = getCenter();
    double area = 0;
    for (int i = 0; i < size() - 1; i++) {
      area += get(i).minus(C).cross(get(i + 1).minus(C)).length() / 2;
    }
    return area;
  }

  public double getFaceAngle(int index) {
    return acos(getVertex(index - 1).minus(getVertex(index)).normalized()
        .dot(getVertex(index + 1).minus(getVertex(index)).normalized()));
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

  public M3d getCenter() {
    M3d pt = new M3d();
    for (Vertex v : this) {
      pt = pt.plus(v);
    }
    return pt.times(1.0 / size());
  }
  
  public M3d getAverageVerticesExcluding(Vertex... toSkip) {
    M3d pt = new M3d();
    int n = 0;
    
    for (Vertex v : this) {
      boolean shouldSkip = false;
      for (Vertex skip : toSkip) {
        if (skip.equals(v)) {
          shouldSkip = true;
          n++;
          break;
        }
      }
      if (!shouldSkip) {
        pt = pt.plus(v);
      }
    }
    if (n != toSkip.length) {
      throw new RuntimeException("Expected to skip " + toSkip.length + " but only skipped " + n + ".");
    }
    return pt.times(1.0 / (size() - n));
  }
  
  public boolean matches(Face other) {
    if (other.hash == hash) {
      return true;
    } else if (other.size() != size()){
      return false;
    } else {
      for (Vertex v : this) {
        boolean matched = false;
        for (Vertex otherV : other) {
          if (v.equals(otherV)) {
            matched = true;
            break;
          }
        }
        if (!matched) {
          return false;
        }
      }
      return true;
    }
  }
  
  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Face && size() == ((Face) obj).size()) {
      Face f = (Face) obj;
      for (int i = 0; i < size(); i++) {
        if (!f.get(i).equals(get(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  private static Vertex[] m3dToVertex(M3d[] m3ds) {
    int i = 0;
    Vertex[] verts = new Vertex[m3ds.length];
    for (M3d m3d : m3ds) {
      verts[i++] = new Vertex(m3d);
    }
    return verts;
  }
}
