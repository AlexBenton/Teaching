package com.bentonian.framework.mesh.implicits;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.material.Material;
import com.bentonian.framework.material.MaterialPrimitive;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M3dPair;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ImplicitSurface extends MaterialPrimitive implements IsRayTraceable {

  private static final M3d WHITE = new M3d(1, 1, 1);
  private static final int DEFAULT_TARGET_LEVEL = 3;
  private static final double DELTA = 0.0001;
  private static final M3d DX = new M3d(DELTA, 0, 0);
  private static final M3d DY = new M3d(0, DELTA, 0);
  private static final M3d DZ = new M3d(0, 0, DELTA);

  class EdgeMap extends HashMap<M3dPair, OctTreeEdge> { }
  class Sample {
    double force;
    M3d color;
    public Sample(double force, M3d color) { this.force = force; this.color = color; }
  }

  private double cutoff = 0.5;
  private int targetLevel = 4;
  private M3d min;
  private double scale;
  private int fx, fy, fz;
  private List<Force> forces = new LinkedList<Force>();
  private WeakHashMap<M3d, Sample> samples;
  private LinkedList<Octree> inProgress;
  private Set<Octree> finished;
  private LinkedList<Octree> roots;
  private EdgeMap[] edgeMapArray;

  private boolean showEdges = false;
  private boolean showNormals = true;
  private boolean showBoxes = false;
  private boolean blendColors = false;
  
  private GLVertexData edgesVao = GLVertexData.beginLineTriangles();
  private GLVertexData boxesVao = GLVertexData.beginLineSegments();
  private GLVertexData surfaceVao = GLVertexData.beginTriangles();

  ////////////////////////////////////////

  public ImplicitSurface(M3d min, M3d max) {
    double dx = max.getX() - min.getX();
    double dy = max.getY() - min.getY();
    double dz = max.getZ() - min.getZ();

    this.min = min;
    this.targetLevel = DEFAULT_TARGET_LEVEL;
    if (dx < dy && dx < dz) {
      scale = dx;
    } else if (dy < dx && dy < dz) {
      scale = dy;
    } else {
      scale = dz;
    }
    fx = (int)Math.ceil(dx / scale);
    fy = (int)Math.ceil(dy / scale);
    fz = (int)Math.ceil(dz / scale);

    reset();
  }

  public void reset() {
    M3d[][][] coords = new M3d[fx+1][fy+1][fz+1];
    Set<M3d> targets = Sets.newHashSet();

    samples = new WeakHashMap<M3d, Sample>();
    inProgress = Lists.newLinkedList();
    finished = Sets.newHashSet();
    roots = new LinkedList<Octree>();
    edgeMapArray = new EdgeMap[10];
    for (Force f : forces) {
      if (f instanceof M3d) {
        targets.add((M3d)f);
      }
    }
    for (int x = 0; x <= fx; x++) {
      for (int y = 0; y <= fy; y++) {
        for (int z = 0; z <= fz; z++) {
          coords[x][y][z] = new M3d(
              min.getX() + x * scale,
              min.getY() + y * scale,
              min.getZ() + z * scale);
        }
      }
    }
    for (int x = 0; x < fx; x++) {
      for (int y = 0; y < fy; y++) {
        for (int z = 0; z < fz; z++) {
          inProgress.add(new Octree(this, 0, coords, x, y, z, targets));
        }
      }
    }
    roots.addAll(inProgress);

    edgesVao.dispose();
    boxesVao.dispose();
    surfaceVao.dispose();
    edgesVao = GLVertexData.beginLineTriangles();
    boxesVao = GLVertexData.beginLineSegments();
    surfaceVao = GLVertexData.beginTriangles();
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
    this.targetLevel = targetLevel;
    reset();
    return this;
  }

  public List<Octree> getRoots() {
    return roots;
  }

  public List<Octree> getInProgress() {
    return inProgress;
  }

  public Set<Octree> getFinished() {
    return finished;
  }

  public Sample sumForces(M3d v) {
    return sumForces(v, true);
  }

  public Sample sumForcesUncached(M3d v) {
    return sumForces(v, false);
  }

  private Sample sumForces(M3d v, boolean updateCache) {
    double sum = 0;
    double summedWeight = 0;
    M3d summedColor = new M3d();
    M3d color;
    Sample sample;

    if (!samples.containsKey(v)) {
      for (Force f : forces) {
        double force = f.F(v);
        double weight = Math.abs(force - cutoff);
        sum += force;
        if (f instanceof HasColor) {
          summedWeight += weight;
          summedColor = summedColor.plus(((HasColor) f).getColor().times(weight));
        }
      }
      color = (summedWeight > 0) ? summedColor.times(1/summedWeight) : WHITE;
      sample = new Sample(sum, color);
      if (updateCache) {
        samples.put(v, sample);
      }
    }
    else {
      sample = samples.get(v);
    }

    return sample;
  }

  boolean isHot(M3d v) {
    return sumForces(v).force > cutoff;
  }

  public ImplicitSurface addForce(Force f) {
    forces.add(f);
    return this;
  }

  public void refine() {
    while (!inProgress.isEmpty()) {
      Octree T = inProgress.remove();
      Octree[][][] children;

      T.refine();
      children = T.getChildOctrees();
      if (children != null) {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
              Octree t = children[i][j][k];

              if (t != null) {
                if (t.getLevel() < targetLevel) {
                  inProgress.addLast(t);
                }
                else if (t.hasPolygons()){
                  finished.add(t);
                }
              }
            }
          }
        }
      }
    }

    HashMap<M3dPair, OctTreeEdge> map = getEdgeMap(getTargetLevel());
    for (OctTreeEdge edge : map.values()) {
      if (edge.isInteresting()) {
        if (edge.size() > 0 && edge.size() < 4) {
          for (Octree o : edge) {
            for (int i = 0; i < 6; i++) {
              if (o.hasInterestingFace(i, edge)) {
                M3d pt = o.getPointJustBeyondFace(i);
                Octree op = findUniqueContainer(pt);

                if ((op != null) && (op.getLevel() < targetLevel)) {
                  op.addTarget(pt);
                  inProgress.add(op);
                }
              }
            }
          }
        }
      }
    }

    if (!inProgress.isEmpty()) {
      refine();
    }
  }

  private boolean addInterpolants(OctTreeEdge edge) {
    M3d a = edge.getEndPt(0);
    M3d b = edge.getEndPt(1);
    Sample ta = sumForces(a);
    Sample tb = sumForces(b);
    Vertex c;
    double t;

    if (ta.force <= cutoff && tb.force > cutoff) {
      t = (cutoff-ta.force) / (tb.force-ta.force);
      c = new Vertex(a.plus(b.minus(a).times(t)), ta.color.plus(tb.color.minus(ta.color).times(t)));
      edge.setCrossingData(c, a.minus(b).normalized());
      return true;
    } else if (tb.force <= cutoff && ta.force > cutoff) {
      t = (cutoff-tb.force) / (ta.force-tb.force);
      c = new Vertex(b.plus(a.minus(b).times(t)), tb.color.plus(ta.color.minus(tb.color).times(t)));
      edge.setCrossingData(c, b.minus(a).normalized());
      return true;
    }
    return false;
  }

  OctTreeEdge getEdge(int level, M3d a, M3d b, M3d precomputedMidPt) {
    EdgeMap map = getEdgeMap(level);
    M3dPair pair = new M3dPair(a,b);
    OctTreeEdge edge = map.get(pair);

    if (edge == null) {
      edge = new OctTreeEdge(null, a, b, precomputedMidPt);
      addInterpolants(edge);
      map.put(pair, edge);
    }
    return edge;
  }

  M3d subdivideEdge(int level, M3d a, M3d b, M3d precomputedMidPt) {
    OctTreeEdge edge = getEdge(level, a, b, precomputedMidPt);

    for (int i = 0; i<2; i++) {
      if (edge.getChild(i) == null) {
        edge.setChild(i, getEdge(level + 1, edge.getEndPt(i), edge.getMidPt(), null));
      }
    }
    return edge.getMidPt();
  }

  M3d subdivideFace(int level, M3d a, M3d b, M3d alpha, M3d beta) {
    return subdivideEdge(level, alpha, beta, subdivideEdge(level, a, b, null));
  }

  M3d subdivideCube(int level, M3d a, M3d b, M3d alpha, M3d beta, M3d alef, M3d bet) {
    return subdivideEdge(level, alef, bet, subdivideEdge(level, alpha, beta, subdivideEdge(level, a, b, null)));
  }

  private EdgeMap getEdgeMap(int level) {
    if (edgeMapArray.length <= level) {
      EdgeMap[] temp = edgeMapArray;

      edgeMapArray = new EdgeMap[level + 5];
      for (int i = 0; i < temp.length; i++) {
        edgeMapArray[i] = temp[i];
      }
    }
    if (edgeMapArray[level] == null) {
      edgeMapArray[level] = new EdgeMap();
    }
    return edgeMapArray[level];
  }

  private List<Octree> findContainer(M3d pt, Octree parent) {
    List<Octree> ret = new LinkedList<Octree>();
    boolean addedChild = false;
    Octree[][][] kids = parent.getChildOctrees();

    if (parent.encloses(pt)) {
      if (kids != null) {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
              if (kids[i][j][k] != null) {
                List<Octree> containingKids = findContainer(pt, kids[i][j][k]);

                if (!containingKids.isEmpty()) {
                  ret.addAll(containingKids);
                  addedChild = true;
                }
              }
            }
          }
        }
      }

      if (!addedChild) {
        ret.add(parent);
      }
    }

    return ret;
  }

  private List<Octree> findContainer(M3d pt) {
    List<Octree> ret = new LinkedList<Octree>();

    for (Octree root : roots) {
      if (root.encloses(pt)) {
        ret.addAll(findContainer(pt, root));
      }
    }
    return ret;
  }

  private Octree findUniqueContainer(M3d pt) {
    List<Octree> containers = findContainer(pt);

    if (containers.size()==1) {
      return containers.get(0);
    } else {
      return null;
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  @Override
  public synchronized void renderLocal(GLCanvas canvas) {

    refine();

    if (showEdges) {
      if (!edgesVao.isCompiled()) {
        edgesVao.color(new M3d(0,0,0));
        for (Octree octree : getFinished()) {
          octree.addEdges(edgesVao);
        }
      }
      edgesVao.render(canvas);
    }

    if (!surfaceVao.isCompiled()) {
      surfaceVao.color(getMaterial().getColor());
      for (Octree octree : getFinished()) {
        octree.addFaces(surfaceVao, showNormals, blendColors);
      }
    }
    surfaceVao.render(canvas);

    if (showBoxes) {
      if (!boxesVao.isCompiled()) {
        boxesVao.color(new M3d(0,0,0));
        for (Octree octree : getFinished()) {
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
    Map<Vertex, Vertex> vertices = Maps.newHashMap();
    for (Octree octree : getFinished()) {
      for (Triangle poly : octree.getPolygonList()) {
        if (!vertices.containsKey(poly.a)) { vertices.put(poly.a, new Vertex(poly.a)); }
        if (!vertices.containsKey(poly.b)) { vertices.put(poly.b, new Vertex(poly.b)); }
        if (!vertices.containsKey(poly.c)) { vertices.put(poly.c, new Vertex(poly.c)); }
        mesh.add(new Face(vertices.get(poly.a), vertices.get(poly.b), vertices.get(poly.c)));
      }
    }
    mesh.computeAllNormals();
    return mesh;
  }

  public boolean getShowNormals() {
    return showNormals;
  }

  public ImplicitSurface setShowNormals(boolean showNormals) {
    this.showNormals = showNormals;
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

  public synchronized void resetAndRefine() {
    reset();
    refine();
  }

  @Override
  public synchronized RayIntersections traceLocal(Ray ray) {
    RayIntersections hits = new RayIntersections();

    for (Octree octree : getRoots()) {
      traceOctree(octree, ray, hits);
    }
    return hits;
  }

  private void traceOctree(Octree octree, Ray ray, RayIntersections hits) {
    if (ray.intersectsCube(octree.getMin(), octree.getMax())) {
      if (!octree.hasPolygons()) {
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
}
