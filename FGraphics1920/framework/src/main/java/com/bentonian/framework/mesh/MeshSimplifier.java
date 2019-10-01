package com.bentonian.framework.mesh;

import static java.util.Comparator.comparingDouble;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.bentonian.framework.math.Vec3;

public class MeshSimplifier {
  
  private final Mesh mesh;
  private final Map<Vec3, List<MeshFace>> candidates;
  
  public MeshSimplifier(Mesh mesh) {
    this.mesh = mesh;
    this.candidates = new HashMap<>();
    for (MeshFace c : mesh) {
      List<MeshFace> list = candidates.get(c.getNormal());
      if (list == null) {
        list = new ArrayList<>();
        candidates.put(c.getNormal(),  list);
      }
      list.add(c);
    }
    for (List<MeshFace> candidateList : candidates.values()) {
      Collections.sort(candidateList, comparingDouble(f -> f.minZ()));
      Collections.sort(candidateList, comparingDouble(f -> f.minY()));
      Collections.sort(candidateList, comparingDouble(f -> f.minX()));
    }
  }
  
  public void simplify() {
    System.out.println("Simplifier initial count: " + count());
    for (List<MeshFace> candidateList : candidates.values()) {
      while (simplifyCandidateSetOnce(candidateList)) { 
        System.out.println("Simplifier count: " + count());
      }
    }
  }

  public boolean simplify(int n) {
    int start = count();
    for (List<MeshFace> candidateList : candidates.values()) {
      while (simplifyCandidateSetOnce(candidateList) && start - count() < n) { 
        System.out.println("Simplifier count: " + count());
      }
    }
    int end = count();
    return start != end;
  }
  
  private boolean simplifyCandidateSetOnce(List<MeshFace> candidates) {
    for (int i = 0; i < candidates.size(); i++) {
      for (int j = 0; j < candidates.size(); j++) {
        if (i != j) {
          MeshFace c = candidates.get(i);
          MeshFace d = candidates.get(j);
          Optional<MeshFace> face = c.mergeIfSimpleAndConvex(d);
          if (face.isPresent()) {
            candidates.remove(c);
            candidates.remove(d);
            mesh.remove(c);
            mesh.remove(d);
            c.detachFromVertices();
            d.detachFromVertices();
            candidates.add(face.get());
            mesh.add(face.get());
            mesh.checkMesh();
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private int count() {
    int sum = 0;
    for (List<MeshFace> candidateList : candidates.values()) {
      sum += candidateList.size();
    }
    return sum;
  }
}
