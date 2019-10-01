package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.material.Colors;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.ui.Vertex;


public class OctreeEdgeInterpolationData {

  private static boolean smoothEdgeInterpolation = true;
  
  final Sample a, b;
  final Vertex interpolatedCrossing;

  OctreeEdgeInterpolationData(Sample a, Sample b, ForceFunction f) {
    double t = smoothEdgeInterpolation ? (f.getCutoff() - a.getForce()) / (b.getForce() - a.getForce()) : 0.5;
    this.a = a;
    this.b = b;
    this.interpolatedCrossing = new Vertex(a.plus(b.minus(a).times(t)));
    
    Vec3 ac = a.getColor().orElse(Colors.WHITE);
    Vec3 bc = a.getColor().orElse(Colors.WHITE);
    this.interpolatedCrossing.setColor(ac.plus(bc.minus(ac).times(t)));
  }
  
  public static void setSmoothEdgeInterpolation(boolean smoothed) {
    smoothEdgeInterpolation = smoothed;
  }

  public static boolean getSmoothEdgeInterpolation() {
    return smoothEdgeInterpolation;
  }

  public Vertex getInterpolatedCrossing() {
    return interpolatedCrossing;
  }

  public boolean hasSharedEndPoint(OctreeEdgeInterpolationData edge) {
    return a.equals(edge.a) || a.equals(edge.b) || b.equals(edge.a) || b.equals(edge.b);
  }
}
