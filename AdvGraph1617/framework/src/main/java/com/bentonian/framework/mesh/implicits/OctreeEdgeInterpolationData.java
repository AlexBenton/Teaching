package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.Vertex;


public class OctreeEdgeInterpolationData {

  final Sample a, b;
  final M3d normalDir;
  final Vertex interpolatedCrossing;

  OctreeEdgeInterpolationData(Sample a, Sample b, double cutoff) {
    double t = (cutoff - a.force) / (b.force - a.force);
    this.a = a;
    this.b = b;
    this.normalDir = ((a.force <= cutoff) ? a.minus(b) : b.minus(a)).normalized();
    this.interpolatedCrossing = new Vertex(a.plus(b.minus(a).times(t)));
    this.interpolatedCrossing.setColor(a.color.plus(b.color.minus(a.color).times(t)));
  }

  public Vertex getInterpolatedCrossing() {
    return interpolatedCrossing;
  }

  public M3d getNormalDir() {
    return normalDir;
  }

  public boolean hasSharedEndPoint(OctreeEdgeInterpolationData edge) {
    return a.equals(edge.a) || a.equals(edge.b) || b.equals(edge.a) || b.equals(edge.b);
  }
}
