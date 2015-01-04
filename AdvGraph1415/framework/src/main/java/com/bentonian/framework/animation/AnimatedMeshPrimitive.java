package com.bentonian.framework.animation;

import java.util.List;
import java.util.Map;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Edge;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.mesh.primitive.MeshPrimitive;
import com.bentonian.framework.ui.GLCanvas;
import com.google.common.collect.Lists;

public class AnimatedMeshPrimitive extends MeshPrimitive {

  private final List<Interpolation> paths;

  private long animationStart = 0;
  private long animationEnd = 0;
  private double startT = 0;
  private double endT = 0;

  public AnimatedMeshPrimitive() {
    super(new Mesh());
    this.paths = Lists.newArrayList();
  }

  public void copyFaces(Mesh source) {
    Map<Vertex, Vertex> newVerts = getMesh().copy(source);
    for (Interpolation interpolant : paths) {
      interpolant.vertex = newVerts.get(interpolant.vertex);
    }
  }

  public Vertex addVertexAnimation(Vertex v, M3d from, M3d to) {
    paths.add(new Interpolation(v, from, to));
    return v;
  }

  public Vertex addVertexAnimation(Vertex v, Edge from, M3d to) {
    paths.add(new Interpolation(v, from.getMidpoint(), to));
    return v;
  }

  public Vertex addVertexAnimation(Vertex v, Face from, M3d to) {
    paths.add(new Interpolation(v, from.getCenter(), to));
    return v;
  }

  public void animate(long durationMillis, double fromT, double toT) {
    setT(fromT);
    animationStart = System.currentTimeMillis();
    animationEnd = animationStart + durationMillis;
    startT = fromT;
    endT = toT;
  }

  public boolean isAnimating() {
    return System.currentTimeMillis() <= animationEnd;
  }

  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    long tick = System.currentTimeMillis();
    if (tick <= animationEnd) {
      double progress = ((tick - animationStart) / (double) (animationEnd - animationStart));
      setT(startT + progress * (endT - startT));
    }
    super.renderLocal(glCanvas);
  }

  private void setT(double t) {
    for (Interpolation interpolant : paths) {
      interpolant.vertex.set(interpolant.interpolate(t));
    }
    getMesh().computeAllNormals();
    dispose();
  }

  private static class Interpolation {
    Vertex vertex;
    M3d from;
    M3d to;

    public Interpolation(Vertex vertex, M3d from, M3d to) {
      this.vertex = vertex;
      this.from = from;
      this.to = to;
    }

    M3d interpolate(double t) {
      return from.times(1-t).plus(to.times(t));
    }
  }
}
