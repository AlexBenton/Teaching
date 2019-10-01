package com.bentonian.framework.animation;

import java.util.List;
import java.util.Map;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.MeshEdge;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshVertex;
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
    Map<MeshVertex, MeshVertex> newVerts = getMesh().copy(source);
    for (Interpolation interpolant : paths) {
      interpolant.vertex = newVerts.get(interpolant.vertex);
    }
  }

  public MeshVertex addVertexAnimation(MeshVertex v, Vec3 from, Vec3 to) {
    paths.add(new Interpolation(v, from, to));
    return v;
  }

  public MeshVertex addVertexAnimation(MeshVertex v, MeshEdge from, Vec3 to) {
    paths.add(new Interpolation(v, from.getMidpoint(), to));
    return v;
  }

  public MeshVertex addVertexAnimation(MeshVertex v, MeshFace from, Vec3 to) {
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
    MeshVertex vertex;
    Vec3 from;
    Vec3 to;

    public Interpolation(MeshVertex vertex, Vec3 from, Vec3 to) {
      this.vertex = vertex;
      this.from = from;
      this.to = to;
    }

    Vec3 interpolate(double t) {
      return from.times(1-t).plus(to.times(t));
    }
  }
}
