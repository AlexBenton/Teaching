package com.bentonian.framework.mesh.implicits;

import static com.bentonian.framework.material.Colors.BLACK;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.bentonian.framework.material.Colors;
import com.bentonian.framework.material.Material;
import com.bentonian.framework.material.MaterialPrimitive;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshVertex;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;
import com.bentonian.framework.ui.Vertex;
import com.google.common.collect.Maps;

public class ImplicitSurfaceMesh extends MaterialPrimitive implements IsRayTraceable {

  private static final double DELTA = 0.0001;
  private static final Vec3 DX = new Vec3(DELTA, 0, 0);
  private static final Vec3 DY = new Vec3(0, DELTA, 0);
  private static final Vec3 DZ = new Vec3(0, 0, DELTA);

  private final ImplicitSurfaceRefiner refiner;

  private boolean showFaces = true;
  private boolean showEdges = false;
  private boolean showNormals = true;
  private boolean showBoxes = false;
  private boolean blendColors = false;

  private final GLVertexData edgesVao = GLVertexData.beginLineSegments();
  private final GLVertexData boxesVao = GLVertexData.beginLineSegments();
  private final GLVertexData surfaceVao = GLVertexData.beginTriangles();

  ////////////////////////////////////////

  public ImplicitSurfaceMesh(ImplicitSurfaceRefiner refiner) {
    this.refiner = refiner;
    reset();
  }

  public ImplicitSurfaceMesh(Vec3 min, Vec3 max, ForceFunction forceFunction) {
    this(new ImplicitSurfaceRefiner(min, max, forceFunction));
  }

  public ImplicitSurfaceMesh reset() {
    refiner.reset();
    dispose();
    return this;
  }

  public int getTargetLevel() {
    return refiner.getTargetLevel();
  }

  public ImplicitSurfaceMesh setTargetLevel(int targetLevel) {
    if (refiner.setTargetLevel(targetLevel)) {
      dispose();
    }
    return this;
  }

  public int getNumPolys() {
    int n = 0;
    for (Octree octree : refiner.getKnownOctrees()) {
      n += octree.getPolygonList().size();
    }
    return n;
  }

  public void dispose() {
    edgesVao.dispose();
    boxesVao.dispose();
    surfaceVao.dispose();
  }

  public ImplicitSurfaceMesh refineCompletely() {
    refiner.refineCompletely();
    return this;
  }
  
  public boolean getShowNormals() {
    return showNormals;
  }

  public ImplicitSurfaceMesh setShowNormals(boolean showNormals) {
    this.showNormals = showNormals;
    return this;
  }

  public boolean getShowFaces() {
    return showFaces;
  }

  public ImplicitSurfaceMesh setShowFaces(boolean showFaces) {
    this.showFaces = showFaces;
    return this;
  }

  public boolean getShowEdges() {
    return showEdges;
  }

  public ImplicitSurfaceMesh setShowEdges(boolean showEdges) {
    this.showEdges = showEdges;
    return this;
  }

  public boolean getShowColors() {
    return blendColors;
  }

  public ImplicitSurfaceMesh setBlendColors(boolean blendColors) {
    this.blendColors = blendColors;
    return this;
  }

  public boolean getShowBoxes() {
    return showBoxes;
  }

  public ImplicitSurfaceMesh setShowBoxes(boolean showBoxes) {
    this.showBoxes = showBoxes;
    return this;
  }

  @Override
  public void render(GLCanvas glCanvas) {
    if (refiner.refine()) {
      dispose();
      if (refiner.isRefined()) {
        // Simplify mesh
      }
    }
    super.render(glCanvas);
  }

  @Override
  public synchronized void renderLocal(GLCanvas canvas) {
    if (showEdges) {
      if (!edgesVao.isCompiled()) {
        edgesVao.color(BLACK);
        for (Octree octree : refiner.getKnownOctrees()) {
          renderEdges(octree);
        }
      }
      edgesVao.render(canvas);
    }

    if (showFaces) {
      if (!surfaceVao.isCompiled()) {
        surfaceVao.color(getMaterial().getColor());
        for (Octree octree : refiner.getKnownOctrees()) {
          renderFaces(octree);
        }
      }
      surfaceVao.render(canvas);
    }

    if (showBoxes) {
      if (!boxesVao.isCompiled()) {
        boxesVao.color(BLACK);
        for (Octree octree : refiner.getKnownOctrees()) {
          if (octree.isInteresting() || octree.getLevel() < refiner.getTargetLevel()) {
            for (int x = 0; x<2; x++) {
              for (int y = 0; y<2; y++) {
                for (int z = 0; z<2; z++) {
                  if (x > 0) {
                    boxesVao.vertex(octree.getCorner(0, y, z));
                    boxesVao.vertex(octree.getCorner(1, y, z));
                  }
                  if (y > 0) {
                    boxesVao.vertex(octree.getCorner(x, 0, z));
                    boxesVao.vertex(octree.getCorner(x, 1, z));
                  }
                  if (z > 0) {
                    boxesVao.vertex(octree.getCorner(x, y, 0));
                    boxesVao.vertex(octree.getCorner(x, y, 1));
                  }
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
    Map<Vec3, MeshVertex> vertices = Maps.newHashMap();
    for (Octree octree : refiner.getKnownOctrees()) {
      for (Triangle poly : octree.getPolygonList()) {
        if (!vertices.containsKey(poly.a)) { vertices.put(poly.a, new MeshVertex(poly.a)); }
        if (!vertices.containsKey(poly.b)) { vertices.put(poly.b, new MeshVertex(poly.b)); }
        if (!vertices.containsKey(poly.c)) { vertices.put(poly.c, new MeshVertex(poly.c)); }
        mesh.add(new MeshFace(vertices.get(poly.a), vertices.get(poly.b), vertices.get(poly.c)));
      }
    }
    mesh.computeAllNormals();
    mesh.checkMesh();
    return mesh;
  }

  @Override
  public synchronized RayIntersections traceLocal(Ray ray) {
    RayIntersections hits = new RayIntersections();

    for (Octree octree : refiner.getRoots()) {
      traceOctree(octree, ray, hits);
    }
    return hits;
  }

  private static class Edge {
    Vertex a;
    Vertex b;
    
    Edge(Vertex a, Vertex b) { this.a = a; this.b = b; }
    @Override public int hashCode() { return a.hashCode() + b.hashCode(); }
    @Override public boolean equals(Object o) { return hashCode() == ((Edge) o).hashCode(); }
  }
  
  private void inc(Map<Edge, Integer> map, Edge e) {
    Integer n = map.get(e);
    map.put(e, ((n == null) ? 0 : n) + 1);
  }

  void renderEdges(Octree octree) {
    Map<Edge, Integer> edges = new HashMap<>();

    for (Triangle poly : octree.getPolygonList()) {
      inc(edges, new Edge(poly.a, poly.b));
      inc(edges, new Edge(poly.b, poly.c));
      inc(edges, new Edge(poly.c, poly.a));
    }
    
    for (Entry<Edge, Integer> edge : edges.entrySet()) {
      if (edge.getValue() == 1) {
        edgesVao.vertex(edge.getKey().a);
        edgesVao.vertex(edge.getKey().b);
      }
    }
  }

  void renderFaces(Octree octree) {
    for (Triangle poly : octree.getPolygonList()) {
      if (showNormals) {
        surfaceVao.normal(poly.normal);
      }
      if (blendColors) {
        surfaceVao.color(poly.a.getColor());
        surfaceVao.vertex(poly.a);
        surfaceVao.color(poly.b.getColor());
        surfaceVao.vertex(poly.b);
        surfaceVao.color(poly.c.getColor());
        surfaceVao.vertex(poly.c);
      } else {
        surfaceVao.vertex(poly.a);
        surfaceVao.vertex(poly.b);
        surfaceVao.vertex(poly.c);
      }
    }
  }

  private void traceOctree(Octree octree, Ray ray, RayIntersections hits) {
    if (ray.intersectsCube(octree.getMin(), octree.getMax())) {
      if (octree.getLevel() < refiner.getTargetLevel()) {
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
        ForceFunction forceFunction = refiner.getForceFunction();
        
        if (forceFunction instanceof ForceFunction) {
          ((ForceFunction) forceFunction).setCaching(false);
        }
        
        for (Triangle poly : octree.getPolygonList()) {
          Double t = ray.intersectsTriangle(poly.a, poly.b, poly.c, poly.normal);
          if (t != null) {
            Vec3 P = ray.at(t);
            Sample sample = forceFunction.sample(P);
            double f = sample.getForce();
            Vec3 approximateNormal = new Vec3(
                f - forceFunction.sample(P.plus(DX)).getForce(),
                f - forceFunction.sample(P.plus(DY)).getForce(),
                f - forceFunction.sample(P.plus(DZ)).getForce()).normalized();
            Material material = new Material(getMaterial());
            if (blendColors) {
              material.setColor(sample.getColor().orElse(Colors.WHITE));
            }
            hits.add(this, t, P, approximateNormal, material);
          }
        }
        
        if (forceFunction instanceof ForceFunction) {
          ((ForceFunction) forceFunction).setCaching(true);
        }
      }
    }
  }
}
