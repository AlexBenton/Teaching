package com.bentonian.framework.mesh;

import static java.lang.Math.acos;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.MathUtil;
import com.bentonian.framework.math.Vec3;

public class MeshFace extends ArrayList<MeshVertex> {

  private static int hashSeed = 0;

  private final int hash;

  private Vec3 normal;

  public MeshFace(Vec3... verts) {
    this(m3dToMeshVertex(verts));
  }

  public MeshFace(MeshVertex... verts) {
    this.hash = hashSeed++;
    for (MeshVertex vert : verts) {
      if (vert == null) {
        throw new NullPointerException("Null Vertex received");
      }
      if (!contains(vert)) {
        add(vert);
      }
    }
    for (int i = 0; i < size(); i++) {
      verts[i].addFace(this, i);
    }
    computeNormal();
  }
  
  public void detachFromVertices() {
    for (MeshVertex v : this) {
      v.removeFace(this);
    }
  }

  public void computeNormal() {
    normal = get(1).minus(get(0)).cross(get(-1).minus(get(0))).normalized();
  }

  public Vec3 getNormal() {
    return normal;
  }

  @Override
  public MeshVertex get(int i) {
    int n = size();
    return super.get(((i % n) + n) % n);
  }
  
  public int findVertex(Vec3 v) {
    for (int i = 0; i < size(); i++) {
      if (get(i).equals(v)) {
        return i;
      }
    }
    return -1;
  }

  public double getArea() {
    Vec3 C = getCenter();
    double area = 0;
    for (int i = 0; i < size() - 1; i++) {
      area += get(i).minus(C).cross(get(i + 1).minus(C)).length() / 2;
    }
    return area;
  }

  public double getFaceAngle(int index) {
    return acos(get(index - 1).minus(get(index)).normalized()
        .dot(get(index + 1).minus(get(index)).normalized()));
  }

  public void getBounds(Vec3 min, Vec3 max) {
    min.set(get(0));
    max.set(get(0));
    for (MeshVertex v : this) {
      min.set(min(min.getX(), v.getX()),
          min(min.getY(), v.getY()),
          min(min.getZ(), v.getZ()));
      max.set(max(max.getX(), v.getX()),
          max(max.getY(), v.getY()),
          max(max.getZ(), v.getZ()));
    }
  }

  public Vec3 getCenter() {
    Vec3 pt = new Vec3();
    for (MeshVertex v : this) {
      pt = pt.plus(v);
    }
    return pt.times(1.0 / size());
  }

  public Vec3 getAverageVerticesExcluding(MeshVertex... toSkip) {
    Vec3 pt = new Vec3();
    int n = 0;

    for (MeshVertex v : this) {
      boolean shouldSkip = false;
      for (MeshVertex skip : toSkip) {
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
  
  public boolean matches(MeshFace other) {
    if (other.hash == hash) {
      return true;
    } else if (other.size() != size()){
      return false;
    } else {
      for (MeshVertex v : this) {
        boolean matched = false;
        for (MeshVertex otherV : other) {
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
  
static int counter = 0;
  public Optional<MeshFace> mergeIfSimpleAndConvex(MeshFace m) {
    if (m.getNormal().dot(getNormal()) > 1.0 - MathConstants.EPSILON) {
      int pos = findFirstSharedVertex(m);
      if (pos != -1) {
        List<Vec3> vertices = new LinkedList<>();
        int k = m.findVertex(get(pos));
        for (int i = 0; i < pos; i++) {
          vertices.add(new Vec3(get(i)));
        }
        for (int i = 0; i < m.size() - 1; i++) {
          vertices.add(new Vec3(m.get(i + k)));
        }
        for (int i = pos + 1; i < size(); i++) {
          vertices.add(new Vec3(get(i)));
        }
        for (int i = 0; i < vertices.size(); i++) {
          if (MathUtil.colinear(vertices.get(i), vertices.get((i + 1) % vertices.size()), vertices.get((i + 2) % vertices.size()))) {
            vertices.remove((i + 1) % vertices.size());
            i = 0;
          }
        }
        for (int i = 0; i < vertices.size(); i++) {
          Vec3 a = vertices.get((i + 0) % vertices.size());
          Vec3 b = vertices.get((i + 1) % vertices.size());
          Vec3 c = vertices.get((i + 2) % vertices.size());
          if (c.minus(b).cross(a.minus(b)).dot(getNormal()) < 0) {
            // Not convex
//System.out.println("Non-convex face generated");
//System.out.println("  a = " + a);
//System.out.println("  b = " + b);
//System.out.println("  c = " + c);
//System.out.println("  n = " + getNormal());
//System.out.println("  cross product = " + c.minus(b).cross(a.minus(b)));
//System.out.println("  dot = " + c.minus(b).cross(a.minus(b)).dot(getNormal()));
//
//System.out.println("Sorted list of local hashcodes: ");
//for (int j = 0; j < size(); j++) {
//  System.out.println("  this(" + j + ") = " + get(j).hashCode());
//}
//System.out.println("Sorted list of remote hashcodes: ");
//for (int j = 0; j < m.size(); j++) {
//  System.out.println("  m(" + j + ") = " + m.get(j).hashCode());
//}
//
//System.out.println("List of joined vertices:");
//for (int j = 0; j < vertices.size(); j++) {
//  Vec3 v = vertices.get(j);
//  System.out.println("  v(" + j + ") = " + v.hashCode() + " = this(" + findVertex(v) + ") and m(" + m.findVertex(v) + ")");
//}
//
//System.out.println("List of cross products:");
//for (int j = 0; j < vertices.size(); j++) {
//  Vec3 a1 = vertices.get((j + 0) % vertices.size());
//  Vec3 b1 = vertices.get((j + 1) % vertices.size());
//  Vec3 c1 = vertices.get((j + 2) % vertices.size());
//  Vec3 n = c1.minus(b1).cross(a1.minus(b1));
//  System.out.println("  n(" + j + ") = " + n);
//}
//
//throw new IllegalArgumentException("Non-convex face found");
            return Optional.empty();
          }
        }
//System.out.println("Merge " + counter++);
        return Optional.of(new MeshFace(vertices.toArray(new Vec3[vertices.size()])));
      }
    }
    return Optional.empty();
  }
  
  private int findFirstSharedVertex(MeshFace m) {
    for (int i = 0; i < size(); i++) {
      if (m.findVertex(get(i)) != -1 && m.findVertex(get(i + 1)) != -1) {
        return i;
      }
    }
    return -1;
  }

  public double minX() {
    double d = iterator().next().getX();
    for (MeshVertex v : this) {
      d = Math.min(d,  v.getX());
    }
    return d;
  }

  public double minY() {
    double d = iterator().next().getY();
    for (MeshVertex v : this) {
      d = Math.min(d,  v.getY());
    }
    return d;
  }

  public double minZ() {
    double d = iterator().next().getZ();
    for (MeshVertex v : this) {
      d = Math.min(d,  v.getZ());
    }
    return d;
  }

  @Override
  public int hashCode() {
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MeshFace && size() == ((MeshFace) obj).size()) {
      MeshFace f = (MeshFace) obj;
      for (int i = 0; i < size(); i++) {
        if (!f.get(i).equals(get(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  private static MeshVertex[] m3dToMeshVertex(Vec3[] m3ds) {
    int i = 0;
    MeshVertex[] verts = new MeshVertex[m3ds.length];
    for (Vec3 m3d : m3ds) {
      verts[i++] = new MeshVertex(m3d);
    }
    return verts;
  }
}
