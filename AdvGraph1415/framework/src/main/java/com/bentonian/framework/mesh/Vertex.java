package com.bentonian.framework.mesh;

import static java.lang.Math.PI;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.texture.TexCoord;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Vertex extends M3d {

  private static final M3d NONE = new M3d(0, 0, 0);
  private static final M3d BLACK = new M3d(0, 0, 0);

  private final Map<Face, Integer> faces = Maps.newHashMap();

  private M3d normal = NONE;
  private M3d color = BLACK;
  private TexCoord tc = null;

  public Vertex() {
  }

  public Vertex(M3d src) {
    super(src);
  }

  public Vertex(double[] coords) {
    super(coords);
  }

  public Vertex(M3d src, M3d color) {
    super(src);
    this.color = color;
  }

  public Vertex(Vertex src) {
    super(src);
    this.color = src.color;
    this.normal = src.normal;
    this.tc = src.tc;
  }

  public Vertex(double x, double y, double z) {
    super(new M3d(x, y, z));
  }

  public void addFace(Face face, int index) {
    faces.put(face, index);
    Preconditions.checkNotNull(faces.get(face));
  }

  public void computeNormal() {
    normal = new M3d();
    for (Entry<Face, Integer> entry : faces.entrySet()) {
      normal = normal.plus(
          entry.getKey().getNormal().times(entry.getKey().getFaceAngle(entry.getValue())));
    }
    normal = normal.normalized();
  }

  public int getFaceIndex(Face face) {
    return faces.get(face);
  }

  public Set<Face> getFaces() {
    return faces.keySet();
  }

  public Edge[] checkForBoundary() {
    for (Entry<Face, Integer> entry : faces.entrySet()) {
      Edge e = new Edge(this, entry.getKey().getVertex(entry.getValue() + 1));
      if (e.isBoundaryEdge()) {
        for (Entry<Face, Integer> otherSide : faces.entrySet()) {
          Edge otherEdge = new Edge(this, otherSide.getKey().getVertex(otherSide.getValue() - 1));
          if (otherEdge.isBoundaryEdge()) {
            return new Edge[] { e, otherEdge };
          }
        }
      }
    }
    return null;
  }

  public Face[] getFaceRingOrdered() {
    Face[] faceList = new Face[faces.size()];
    Face curr = null;

    for (Entry<Face, Integer> entry : faces.entrySet()) {
      Edge e = new Edge(this, entry.getKey().getVertex(entry.getValue() + 1));
      if (e.isBoundaryEdge()) {
        curr = entry.getKey();
      }
    }
    if (curr == null) {
      curr = faces.keySet().iterator().next();
    }

    faceList[0] = curr;
    for (int i = 1; i < faces.size(); i++) {
      Vertex neighbor = curr.getVertex(getFaceIndex(curr) - 1);
      curr = (new Edge(this, neighbor)).getOtherFace(curr);
      if (curr == null) {
        throw new RuntimeException("Shouldn't have run out of adjacent faces");
      }
      faceList[i] = curr;
    }
    return faceList;
  }

  public Set<Vertex> getOneRing() {
    Set<Vertex> oneRing = Sets.newHashSet();
    for (Entry<Face, Integer> entry : faces.entrySet()) {
      oneRing.add(entry.getKey().getVertex(entry.getValue() - 1));
      oneRing.add(entry.getKey().getVertex(entry.getValue() + 1));
    }
    return oneRing;
  }

  public double getAngleDeficit() {
    double faceAngle = 0;
    for (Entry<Face, Integer> entry : faces.entrySet()) {
      faceAngle += entry.getKey().getFaceAngle(entry.getValue());
    }
    return 2 * PI - faceAngle;
  }

  public void setColor(M3d color) {
    this.color = color;
  }

  public M3d getColor() {
    return color;
  }

  public void setNormal(M3d normal) {
    this.normal = normal;
  }

  public M3d getNormal() {
    return normal;
  }

  public Vertex setTextureCoords(TexCoord tc) {
    this.tc = tc;
    return this;
  }

  public TexCoord getTexCoords() {
    return tc;
  }
}
