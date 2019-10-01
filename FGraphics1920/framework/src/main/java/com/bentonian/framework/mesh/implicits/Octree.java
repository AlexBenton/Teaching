package com.bentonian.framework.mesh.implicits;

import static com.bentonian.framework.math.MathUtil.midPt;
import static com.bentonian.framework.mesh.implicits.OctreeConstants.FACES;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshUtil;
import com.bentonian.framework.ui.Vertex;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


public class Octree {

  public enum State {
    UNEXAMINED,
    REFINED,
    SCHEDULED_FOR_REFINEMENT,
    SCHEDULED_FOR_MISSED_FACES_CHECK,
    FINISHED
  }
  
  public static boolean USE_CUBES = true;

  private ForceFunction forceFunction;
  private int level;
  private Sample[][][] corners;
  private Set<Vec3> targets;
  private Octree parent;
  private Octree[][][] children;
  private List<Triangle> polygonList;
  private boolean isOdd;
  private State state = State.UNEXAMINED;

  Octree(ForceFunction forceFunction, Octree parent, int level, Sample[][][] coords,
      int x, int y, int z, Set<Vec3> possibleTargets) {
    this.isOdd = (((x+y+z)&1) != 0);
    this.forceFunction = forceFunction;
    this.parent = parent;
    this.level = level;
    this.corners = new Sample[2][2][2];
    this.targets = Sets.newHashSet();
    this.polygonList = null;

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 2; k++) {
          corners[i][j][k] = coords[x+i][y+j][z+k];
        }
      }
    }

    for (Vec3 pt : possibleTargets) {
      if (encloses(pt)) {
        addTarget(pt);
      }
    }
  }

  /**
   * The hardcoded cube constants were imported from an older project, and their corner indices 
   * don't match this Java project's indexing.  From inspection the mapping is:
   *   000 --> bit 1 set
   *   001 --> bit 2 set
   *   010 --> bit 8 set
   *   011 --> bit 7 set
   *   100 --> bit 4 set
   *   101 --> bit 3 set
   *   110 --> bit 5 set
   *   111 --> bit 6 set
   */
  private static int mapOldCodeCornerBitcodeToCurrentBitcode(int i, int j, int k) {
    int bitcode = (i << 2) | (j << 1) | (k << 0);
    switch (bitcode) {
    case 0: return 1;
    case 1: return 2;
    case 2: return 8;
    case 3: return 7;
    case 4: return 4;
    case 5: return 3;
    case 6: return 5;
    case 7: return 6;
    default: throw new IllegalAccessError();
    }
  }

  private void polygonalize() {
    if (USE_CUBES) {
      int bitsHot = 0;
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          for (int k = 0; k < 2; k++) {
            if (forceFunction.isHot(corners[i][j][k])) {
              bitsHot |= (1 << (mapOldCodeCornerBitcodeToCurrentBitcode(i, j, k) - 1));
            }
          }
        }
      }
      
      int[] edgeIndices = OctreeCubeConstants.CUBE_EDGE_INDICES[bitsHot];
      for (int tri = 0; tri < edgeIndices.length; tri += 3) {
        OctreeEdgeInterpolationData edgeA = findEdge(edgeIndices[tri + 0]);
        OctreeEdgeInterpolationData edgeB = findEdge(edgeIndices[tri + 1]);
        OctreeEdgeInterpolationData edgeC = findEdge(edgeIndices[tri + 2]);
  
        addPolyWithOrientation(
            edgeA.getInterpolatedCrossing(),
            edgeB.getInterpolatedCrossing(),
            edgeC.getInterpolatedCrossing(),
            getNormalHint(edgeA, edgeB, edgeC));
      }
    } else {
      for (int i = 0; i < OctreeConstants.TETRAHEDRA.length; i++) {
        int crossings = 0;
        OctreeEdgeInterpolationData[] arr = { null, null, null, null };
        int[][] tet = OctreeConstants.TETRAHEDRA[i];
  
        for (int j = 0; j < tet.length; j++) {
          for (int k = j+1; k < tet.length; k++) {
            Sample A = corners [tet[j][0]] [isOdd ? (1 - tet[j][1]) : tet[j][1]] [tet[j][2]];
            Sample B = corners [tet[k][0]] [isOdd ? (1 - tet[k][1]) : tet[k][1]] [tet[k][2]];
            OctreeEdgeInterpolationData interestingEdge = findEdge(A, B);
  
            if (interestingEdge != null) {
              arr[crossings++] = interestingEdge;
            }
          }
        }
  
        addPolygonsFromInterestingEdges(arr, crossings);
      }
    }
  }
  
  public OctreeEdgeInterpolationData findEdge(int e) {
    int[][] ee = OctreeConstants.EDGES[e];
    int[] a = ee[0];
    int[] b = ee[1];
    Sample A = corners [a[0]][a[1]][a[2]];
    Sample B = corners [b[0]][b[1]][b[2]];
    assert forceFunction.isHot(A) != forceFunction.isHot(B);
    return new OctreeEdgeInterpolationData(A, B, forceFunction);
  }

  private void addPolygonsFromInterestingEdges(OctreeEdgeInterpolationData[] arr, int crossings) {
    if (crossings == 4) {
      boolean zeroOneAdjacent = arr[0].hasSharedEndPoint(arr[1]);
      OctreeEdgeInterpolationData a = arr[0];
      OctreeEdgeInterpolationData b = zeroOneAdjacent ? arr[1] : arr[2];
      OctreeEdgeInterpolationData c = zeroOneAdjacent ? arr[2] : arr[1];
      OctreeEdgeInterpolationData d = arr[3];

      addPolyWithOrientation(
          a.getInterpolatedCrossing(),
          b.getInterpolatedCrossing(),
          c.getInterpolatedCrossing(),
          getNormalHint(a, b, c));
      addPolyWithOrientation(
          b.getInterpolatedCrossing(),
          c.getInterpolatedCrossing(),
          d.getInterpolatedCrossing(),
          getNormalHint(b, c, d));
    } else if (crossings == 3) {
      addPolyWithOrientation(
          arr[0].getInterpolatedCrossing(),
          arr[1].getInterpolatedCrossing(),
          arr[2].getInterpolatedCrossing(),
          getNormalHint(arr[0], arr[1], arr[2]));
    } else if (crossings > 0) {
      System.err.println("Unexpected numCrossings: " + crossings);
    }
  }

  public Vec3 getNormalHint(OctreeEdgeInterpolationData edge) {
    if (edge.a.getNormal().isPresent() && edge.b.getNormal().isPresent()) {
      // If normals were computed per sample then use those
      return edge.a.getNormal().get().plus(edge.b.getNormal().get()).times(0.5);
    } else {
      // Else, approximate normal hint from force field direction
      return forceFunction.isHot(edge.a) ? edge.b.minus(edge.a) : edge.a.minus(edge.b); 
    }
  }
  
  public boolean hasPolygons() {
    return !getPolygonList().isEmpty();
  }

  public List<Triangle> getPolygonList() {
    if (polygonList == null) {
      polygonList = Lists.newArrayList();
      polygonalize();
    }
    return polygonList;
  }

  /**
   * Caution: does not handle non-simple polygonalizations (ie., two opposite hot corners)
   */
  public MeshFace getPolysAsMeshFace() {
    List<Vertex> edgeVertexPairs = new LinkedList<>();

    for (Triangle poly : getPolygonList()) {
      edgeVertexPairs.add(poly.a); edgeVertexPairs.add(poly.b);
      edgeVertexPairs.add(poly.b); edgeVertexPairs.add(poly.c);
      edgeVertexPairs.add(poly.c); edgeVertexPairs.add(poly.a);
    }
    
    List<Vertex> loop = MeshUtil.simplifyToLoop(edgeVertexPairs);
    MeshUtil.ensureCCW(loop, polygonList.iterator().next().normal);
    return new MeshFace(loop.toArray(new Vertex[loop.size()]));
  }

  public void addTarget(Vec3 pt) {
    targets.add(pt);
  }

  public boolean encloses(Vec3 pt) {
    return pt.getX() >= corners[0][0][0].getX() && pt.getX() <= corners[1][1][1].getX()
        && pt.getY() >= corners[0][0][0].getY() && pt.getY() <= corners[1][1][1].getY()
        && pt.getZ() >= corners[0][0][0].getZ() && pt.getZ() <= corners[1][1][1].getZ();
  }

  public boolean isInteresting() {
    if (!targets.isEmpty()) {
      return true;
    } else {
      boolean first = forceFunction.isHot(corners[0][0][0]);

      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          for (int k = 0; k < 2; k++) {
            if (forceFunction.isHot(corners[i][j][k]) != first) {
              return true;
            }
          }
        }
      }
      return false;
    }
  }

  public boolean hasInterestingFace(int faceIndex) {
    int[][] faceCorners = FACES[faceIndex];
    boolean first = forceFunction.isHot(corners[faceCorners[0][0]][faceCorners[0][1]][faceCorners[0][2]]);

    for (int i = 1; i < 4; i++) {
      if (forceFunction.isHot(corners[faceCorners[i][0]][faceCorners[i][1]][faceCorners[i][2]]) != first) {
        return true;
      }
    }
    return false;
  }

  Vec3 getPointJustBeyondFace(int faceIndex) {
    Vec3 faceCenter = new Vec3();
    Vec3 center = midPt(corners[0][0][0], corners[1][1][1]);

    for (int i = 0; i < 4; i++) {
      faceCenter = faceCenter.plus(getCorner(FACES[faceIndex][i]));
    }
    faceCenter = faceCenter.times(0.25);
    return center.plus(faceCenter.minus(center).times(1.1));
  }

  public int getLevel() {
    return level;
  }

  public Vec3 getMin() {
    return corners[0][0][0];
  }

  public Vec3 getMax() {
    return corners[1][1][1];
  }

  State getState() {
    return state;
  }

  void setState(State state) {
    this.state = state;
  }

  Octree getParent() {
    return parent;
  }

  public Sample getCorner(int i, int j, int k) {
    return corners[i][j][k];
  }

  public Sample getCorner(int[] index) {
    return corners[index[0]][index[1]][index[2]];
  }

  Octree[][][] getChildOctrees() {
    return children;
  }

  void addChildren(Sample[][][] subCorners) {
    if (children == null) {
      children = new Octree[2][2][2];
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          for (int k = 0; k < 2; k++) {
            children[i][j][k] = 
                new Octree(forceFunction, this, level + 1, subCorners, i, j, k, targets);
          }
        }
      }
    }
  }
  
  private OctreeEdgeInterpolationData findEdge(Sample a, Sample b) {
    return (forceFunction.isHot(a) != forceFunction.isHot(b)) ? 
        new OctreeEdgeInterpolationData(a, b, forceFunction) : null;
  }
  
  private Vec3 getNormalHint(
      OctreeEdgeInterpolationData edge1, 
      OctreeEdgeInterpolationData edge2, 
      OctreeEdgeInterpolationData edge3) {
    return getNormalHint(edge1).plus(getNormalHint(edge2)).plus(getNormalHint(edge3)).times(0.33333);
  }

  private void addPolyWithOrientation(Vertex a, Vertex b, Vertex c, Vec3 normalHint) {
    if (!a.equals(b) && !a.equals(c) && !b.equals(c)) {
      Vec3 n = c.minus(b).cross(a.minus(b));

      if (n.dot(normalHint) < 0) {
        n = n.neg();
      }
      polygonList.add(new Triangle(a, b, c, n.normalized()));
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.deepHashCode(corners);
    result = prime * result + level;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Octree)
        && Arrays.deepEquals(corners, ((Octree) obj).corners)
        && level != ((Octree) obj).level;
  }

  @Override
  public String toString() {
    return "L" + level + ": [" + corners[0][0][0] + "] --> [" + corners[1][1][1] + "]";
  }
}