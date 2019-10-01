package com.bentonian.framework.mesh;

import static java.lang.Math.PI;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.ui.Vertex;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class MeshVertex extends Vertex {

  private final Map<MeshFace, Integer> faces = Maps.newHashMap();

  public MeshVertex() {
  }

  public MeshVertex(Vec3 src) {
    super(src);
  }

  public MeshVertex(double[] coords) {
    super(coords);
  }

  public MeshVertex(MeshVertex src) {
    super(src);
  }

  public MeshVertex(double x, double y, double z) {
    super(x, y, z);
  }

  public void addFace(MeshFace face, int index) {
    faces.put(face, index);
    Preconditions.checkNotNull(faces.get(face));
  }

  public void removeFace(MeshFace face) {
    faces.remove(face);
  }

  public void computeNormal() {
    Vec3 n = new Vec3();
    for (Entry<MeshFace, Integer> entry : faces.entrySet()) {
      n = n.plus(entry.getKey().getNormal().times(entry.getKey().getFaceAngle(entry.getValue())));
    }
    setNormal(n.normalized());
  }

  public int getFaceIndex(MeshFace face) {
    return Optional.ofNullable(faces.get(face)).orElse(-1);
  }

  public Set<MeshFace> getFaces() {
    return faces.keySet();
  }

  public MeshEdge[] checkForBoundary() {
    for (Entry<MeshFace, Integer> entry : faces.entrySet()) {
      MeshEdge e = new MeshEdge(this, entry.getKey().get(entry.getValue() + 1));
      if (e.isBoundaryEdge()) {
        for (Entry<MeshFace, Integer> otherSide : faces.entrySet()) {
          MeshEdge otherEdge = new MeshEdge(this, otherSide.getKey().get(otherSide.getValue() - 1));
          if (otherEdge.isBoundaryEdge()) {
            return new MeshEdge[] { e, otherEdge };
          }
        }
      }
    }
    return null;
  }

  public MeshFace[] getFaceRingOrdered() {
    MeshFace[] faceList = new MeshFace[faces.size()];
    MeshFace curr = null;

    for (Entry<MeshFace, Integer> entry : faces.entrySet()) {
      MeshEdge e = new MeshEdge(this, entry.getKey().get(entry.getValue() + 1));
      if (e.isBoundaryEdge()) {
        curr = entry.getKey();
      }
    }
    if (curr == null) {
      curr = faces.keySet().iterator().next();
    }

    faceList[0] = curr;
    for (int i = 1; i < faces.size(); i++) {
      MeshVertex neighbor = curr.get(getFaceIndex(curr) - 1);
      curr = (new MeshEdge(this, neighbor)).getOtherFace(curr);
      if (curr == null) {
        throw new RuntimeException("Shouldn't have run out of adjacent faces");
      }
      faceList[i] = curr;
    }
    return faceList;
  }

  public Set<MeshVertex> getOneRing() {
    Set<MeshVertex> oneRing = Sets.newHashSet();
    for (Entry<MeshFace, Integer> entry : faces.entrySet()) {
      oneRing.add(entry.getKey().get(entry.getValue() - 1));
      oneRing.add(entry.getKey().get(entry.getValue() + 1));
    }
    return oneRing;
  }

  public double getAngleDeficit() {
    double faceAngle = 0;
    for (Entry<MeshFace, Integer> entry : faces.entrySet()) {
      faceAngle += entry.getKey().getFaceAngle(entry.getValue());
    }
    return 2 * PI - faceAngle;
  }
}
