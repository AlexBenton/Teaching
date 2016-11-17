package com.bentonian.framework.mesh.implicits;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.bentonian.framework.material.Colors;
import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.material.Material;
import com.bentonian.framework.material.MaterialPrimitive;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshVertex;
import com.bentonian.framework.mesh.implicits.Octree.State;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ImplicitSurface extends MaterialPrimitive implements IsRayTraceable {

  private static final int DEFAULT_TARGET_LEVEL = 3;
  private static final double DELTA = 0.0001;
  private static final M3d DX = new M3d(DELTA, 0, 0);
  private static final M3d DY = new M3d(0, DELTA, 0);
  private static final M3d DZ = new M3d(0, 0, DELTA);
  private static final long REFINE_TIME_PER_FRAME_MILLIS = 250;

  private final LinkedList<Octree> inProgress;
  private final List<Octree> almostFinished;
  private final List<Octree> finished;
  private final List<Octree> roots;
  private final WeakHashMap<M3d, Sample> samples;

  private double cutoff = 0.5;
  private int targetLevel;
  private M3d min;
  private double scale;
  private int fx, fy, fz;
  private List<Force> forces = new LinkedList<Force>();

  private boolean showFaces = true;
  private boolean showEdges = false;
  private boolean showNormals = true;
  private boolean showBoxes = false;
  private boolean blendColors = false;

  private final GLVertexData edgesVao = GLVertexData.beginLineTriangles();
  private final GLVertexData boxesVao = GLVertexData.beginLineSegments();
  private final GLVertexData surfaceVao = GLVertexData.beginTriangles();

  ////////////////////////////////////////

  public ImplicitSurface(M3d min, M3d max) {
    double dx = max.getX() - min.getX();
    double dy = max.getY() - min.getY();
    double dz = max.getZ() - min.getZ();

    this.min = min;
    this.targetLevel = DEFAULT_TARGET_LEVEL;
    this.inProgress = Lists.newLinkedList();
    this.almostFinished = Lists.newArrayList();
    this.finished = Lists.newLinkedList();
    this.roots = Lists.newLinkedList();
    this.samples = new WeakHashMap<M3d, Sample>();
    this.scale = (dx < dy && dx < dz) ? dx : (dy < dx && dy < dz) ? dy : dz;
    this.fx = (int) Math.ceil(dx / scale);
    this.fy = (int) Math.ceil(dy / scale);
    this.fz = (int) Math.ceil(dz / scale);

    reset();
  }

  public ImplicitSurface reset() {
    Sample[][][] initialSamples = new Sample[fx+1][fy+1][fz+1];
    Set<M3d> targets = Sets.newHashSet();

    samples.clear();
    inProgress.clear();
    almostFinished.clear();
    finished.clear();
    roots.clear();
    for (Force f : forces) {
      if (f instanceof M3d) {
        targets.add((M3d)f);
      }
    }
    for (int x = 0; x <= fx; x++) {
      for (int y = 0; y <= fy; y++) {
        for (int z = 0; z <= fz; z++) {
          initialSamples[x][y][z] = sumForces(new M3d(
              min.getX() + x * scale,
              min.getY() + y * scale,
              min.getZ() + z * scale));
        }
      }
    }
    for (int x = 0; x < fx; x++) {
      for (int y = 0; y < fy; y++) {
        for (int z = 0; z < fz; z++) {
          scheduleRefinement(new Octree(this, null, 0, initialSamples, x, y, z, targets));
        }
      }
    }
    roots.addAll(inProgress);

    dispose();
    return this;
  }

  private void scheduleRefinement(Octree octree) {
    inProgress.addLast(octree);
    octree.setState(State.SCHEDULED_FOR_REFINEMENT);
  }

  public ImplicitSurface refineCompletely() {
    while (!inProgress.isEmpty()) {
      refine();
    }
    return this;
  }

  public double getCutoff() {
    return cutoff;
  }

  public void setCutoff(double cutoff) {
    this.cutoff = cutoff;
  }

  public int getTargetLevel() {
    return targetLevel;
  }

  public ImplicitSurface setTargetLevel(int targetLevel) {
    if (this.targetLevel != targetLevel) {
      this.targetLevel = targetLevel;
      resetInProgressAndFinished();
      dispose();
    }
    return this;
  }

  public int getNumPolys() {
    int n = 0;
    for (Octree octree : getRenderableOctrees()) {
      n += octree.getPolygonList().size();
    }
    return n;
  }

  public ImplicitSurface addForce(Force f) {
    forces.add(f);
    return this;
  }

  private void refine() {
    long timeout = System.currentTimeMillis() + REFINE_TIME_PER_FRAME_MILLIS;
    int numDone = finished.size();

    while (!inProgress.isEmpty() && System.currentTimeMillis() < timeout) {
      refineNext();
    }

    if (inProgress.isEmpty() && !almostFinished.isEmpty()) {
      checkForMissedFaces();
    }

    if (numDone != finished.size()) {
      dispose();
    }
  }

  boolean isHot(Sample sample) {
    return sample.force > cutoff;
  }

  Sample sumForces(M3d v) {
    return sumForces(v, true);
  }

  private Sample sumForcesUncached(M3d v) {
    return sumForces(v, false);
  }

  private Sample sumForces(M3d v, boolean updateCache) {
    double sum = 0;
    double summedWeight = 0;
    M3d summedColor = new M3d();
    M3d color;
    Sample sample = samples.get(v);

    if (sample == null) {
      for (Force f : forces) {
        double force = f.F(v);
        double weight = Math.abs(force - cutoff);
        sum += force;
        if (f instanceof HasColor) {
          summedWeight += weight;
          summedColor = summedColor.plus(((HasColor) f).getColor().times(weight));
        }
      }
      color = (summedWeight > 0) ? summedColor.times(1 / summedWeight) : Colors.WHITE;
      sample = new Sample(v, sum, color);
      if (updateCache) {
        samples.put(v, sample);
      }
    }

    return sample;
  }

  private void refineNext() {
    Octree octree = inProgress.remove();

    octree.refine();
    octree.setState(State.REFINED);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 2; k++) {
          Octree child = octree.getChildOctrees()[i][j][k];
          if (child.getLevel() < targetLevel && child.isInteresting()) {
            scheduleRefinement(child);
          } else if (child.hasPolygons()) {
            finished.add(child);
            almostFinished.add(child);
            child.setState(State.SCHEDULED_FOR_MISSED_FACES_CHECK);
          }
        }
      }
    }
  }

  private void checkForMissedFaces() {
    for (Octree octree : almostFinished) {
      octree.setState(State.FINISHED);
      for (int face = 0; face < 6; face++) {
        if (octree.hasInterestingFace(face)) {
          M3d pt = octree.getPointJustBeyondFace(face);
          Octree op = findContainer(octree, pt);

          if (op != null 
              && op.getLevel() < octree.getLevel() 
              && !op.getState().equals(State.SCHEDULED_FOR_REFINEMENT)) {
            op.addTarget(pt);
            scheduleRefinement(op);
          }
        }
      }
    }
    almostFinished.clear();
  }

  private Octree findContainer(Octree cousin, M3d pt) {
    Octree ancestor = cousin.getParent();
    while (ancestor != null) {
      if (ancestor.encloses(pt)) {
        return findContainerInChildren(pt, ancestor);
      } else {
        ancestor = ancestor.getParent();
      }
    }
    return findContainerFromAllRoots(pt);
  }

  private Octree findContainerFromAllRoots(M3d pt) {
    for (Octree root : roots) {
      if (root.encloses(pt)) {
        Octree container = findContainerInChildren(pt, root);
        if (container != null) {
          return container;
        }
      }
    }
    return null;
  }

  private Octree findContainerInChildren(M3d pt, Octree parent) {
    Octree[][][] kids = parent.getChildOctrees();

    if (parent.encloses(pt)) {
      if (kids != null) {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
              if (kids[i][j][k] != null) {
                Octree container = findContainerInChildren(pt, kids[i][j][k]);
                if (container != null) {
                  return container;
                }
              }
            }
          }
        }
      }

      return parent;
    }

    return null;
  }

  /////////////////////////////////////////////////////////////////////////////

  @Override
  public void render(GLCanvas glCanvas) {
    refine();
    super.render(glCanvas);
  }

  @Override
  public synchronized void renderLocal(GLCanvas canvas) {
    if (showEdges) {
      if (!edgesVao.isCompiled()) {
        edgesVao.color(new M3d(0,0,0));
        for (Octree octree : getRenderableOctrees()) {
          octree.renderEdges(edgesVao);
        }
      }
      edgesVao.render(canvas);
    }

    if (showFaces) {
      if (!surfaceVao.isCompiled()) {
        surfaceVao.color(getMaterial().getColor());
        for (Octree octree : getRenderableOctrees()) {
          octree.renderFaces(surfaceVao, showNormals, blendColors);
        }
      }
      surfaceVao.render(canvas);
    }

    if (showBoxes) {
      if (!boxesVao.isCompiled()) {
        boxesVao.color(new M3d(0,0,0));
        for (Octree octree : getRenderableOctrees()) {
          for (int x = 0; x<2; x++) {
            for (int y = 0; y<2; y++) {
              for (int z = 0; z<2; z++) {
                if (x > 0) {
                  boxesVao.vertex(octree.getCorners()[0][y][z]);
                  boxesVao.vertex(octree.getCorners()[1][y][z]);
                }
                if (y > 0) {
                  boxesVao.vertex(octree.getCorners()[x][0][z]);
                  boxesVao.vertex(octree.getCorners()[x][1][z]);
                }
                if (z > 0) {
                  boxesVao.vertex(octree.getCorners()[x][y][0]);
                  boxesVao.vertex(octree.getCorners()[x][y][1]);
                }
              }
            }
          }
        }
      }
      boxesVao.render(canvas);
    }
  }

  public Mesh getMesh() {
    Mesh mesh = new Mesh();
    Map<M3d, MeshVertex> vertices = Maps.newHashMap();
    for (Octree octree : getRenderableOctrees()) {
      for (Triangle poly : octree.getPolygonList()) {
        if (!vertices.containsKey(poly.a)) { vertices.put(poly.a, new MeshVertex(poly.a)); }
        if (!vertices.containsKey(poly.b)) { vertices.put(poly.b, new MeshVertex(poly.b)); }
        if (!vertices.containsKey(poly.c)) { vertices.put(poly.c, new MeshVertex(poly.c)); }
        mesh.add(new MeshFace(vertices.get(poly.a), vertices.get(poly.b), vertices.get(poly.c)));
      }
    }
    mesh.computeAllNormals();
    return mesh;
  }

  public void dispose() {
    edgesVao.dispose();
    boxesVao.dispose();
    surfaceVao.dispose();
  }

  public boolean getShowNormals() {
    return showNormals;
  }

  public ImplicitSurface setShowNormals(boolean showNormals) {
    this.showNormals = showNormals;
    return this;
  }

  public boolean getShowFaces() {
    return showFaces;
  }

  public ImplicitSurface setShowFaces(boolean showFaces) {
    this.showFaces = showFaces;
    return this;
  }

  public boolean getShowEdges() {
    return showEdges;
  }

  public ImplicitSurface setShowEdges(boolean showEdges) {
    this.showEdges = showEdges;
    return this;
  }

  public boolean getShowColors() {
    return blendColors;
  }

  public ImplicitSurface setBlendColors(boolean blendColors) {
    this.blendColors = blendColors;
    return this;
  }

  public boolean getShowBoxes() {
    return showBoxes;
  }

  public ImplicitSurface setShowBoxes(boolean showBoxes) {
    this.showBoxes = showBoxes;
    return this;
  }

  @Override
  public synchronized RayIntersections traceLocal(Ray ray) {
    RayIntersections hits = new RayIntersections();

    for (Octree octree : roots) {
      traceOctree(octree, ray, hits);
    }
    return hits;
  }

  private void resetInProgressAndFinished() {
    inProgress.clear();
    almostFinished.clear();
    finished.clear();
    for (Octree octree : roots) {
      udpateInProgressAndFinished(octree);
    }
  }

  private void udpateInProgressAndFinished(Octree octree) {
    if (octree.getLevel() < targetLevel) {
      if (octree.getChildOctrees() == null) {
        inProgress.add(octree);
        octree.setState(State.SCHEDULED_FOR_REFINEMENT);
      } else {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
              Octree child = octree.getChildOctrees()[i][j][k];
              if (child != null) {
                udpateInProgressAndFinished(child);
              }
            }
          }
        }
      }
    } else if (octree.getLevel() == targetLevel) {
      finished.add(octree);
    }
  }

  private void traceOctree(Octree octree, Ray ray, RayIntersections hits) {
    if (ray.intersectsCube(octree.getMin(), octree.getMax())) {
      if (octree.getLevel() < targetLevel) {
        if (octree.getChildOctrees() != null) {
          for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
              for (int k = 0; k < 2; k++) {
                Octree child = octree.getChildOctrees()[i][j][k];
                if (child != null) {
                  traceOctree(child, ray, hits);
                }
              }
            }
          }
        }
      } else {
        for (Triangle poly : octree.getPolygonList()) {
          Double t = ray.intersectsTriangle(poly.a, poly.b, poly.c, poly.normal);
          if (t != null) {
            M3d P = ray.at(t);
            Sample sample = sumForces(P);
            double f = sample.force;
            M3d approximateNormal = new M3d(
                f - sumForcesUncached(P.plus(DX)).force,
                f - sumForcesUncached(P.plus(DY)).force,
                f - sumForcesUncached(P.plus(DZ)).force).normalized();
            Material material = new Material(getMaterial());
            if (blendColors) {
              material.setColor(sample.color);
            }
            hits.add(this, t, P, approximateNormal, material);
          }
        }
      }
    }
  }

  private Iterable<Octree> getRenderableOctrees() {
    return Iterables.concat(inProgress, finished);
  }
}
