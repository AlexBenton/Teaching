package com.bentonian.framework.mesh;

import static java.lang.Math.PI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Vertex extends SimpleVertex {

  private static final M3d NONE = new M3d(0, 0, 0);

  private Map<Face, Integer> faces = new HashMap<Face, Integer>();
  private M3d normal = new M3d();
  private TexCoord tc;

  public Vertex(M3d src) {
    super(src);
    this.normal = NONE;
    this.tc = null;
  }

  public Vertex(Vertex src) {
    super(src);
    this.normal = src.normal;
    this.tc = src.tc;
  }

  public Vertex(double x, double y, double z) {
    super(new M3d(x, y, z));
    this.normal = NONE;
  }

  public void addFace(Face face, int index) {
    faces.put(face, index);
  }

  public void computeNormal() {
    normal = new M3d();
    for (Face face : faces.keySet()) {
      normal = normal.plus(face.getNormal().times(face.getFaceAngle(this)));
    }
    normal = normal.normalized();
  }

  public int getFaceIndex(Face f) {
    return faces.get(f);
  }
  
  public Set<Face> getFaces() {
    return faces.keySet();
  }
  
  public Set<Vertex> getOneRing() {
    Set<Vertex> neighbors = Sets.newHashSet();
    for (Entry<Face, Integer> faceEntry : faces.entrySet()) {
      neighbors.add(faceEntry.getKey().getVertex(faceEntry.getValue() - 1));
      neighbors.add(faceEntry.getKey().getVertex(faceEntry.getValue() + 1));
    }
    return neighbors;
  }
  
  public List<Vertex> checkForEdge(Set<Vertex> oneRing) {
    List<Vertex> edgeNeighbors = Lists.newArrayList();
    for (Vertex neighbor : oneRing) {
      if (Sets.intersection(getFaces(), neighbor.getFaces()).size() != 2) {
        edgeNeighbors.add(neighbor);
      }
    }
    return edgeNeighbors;
  }
  
  public double getAngleDeficit() {
    double faceAngle = 0;
    for (Face face : faces.keySet()) {
      faceAngle += face.getFaceAngle(this);
    }
    return 2 * PI - faceAngle;
  }

  public void setNormal(M3d normal) {
    this.normal = normal;
  }

  public M3d getNormal() {
    return normal;
  }
  
  public void setTextureCoords(TexCoord tc) {
    this.tc = tc;
  }
  
  public TexCoord getTexCoords() {
    return tc;
  }
}
