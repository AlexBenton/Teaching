package com.bentonian.framework.mesh.implicits;

import static com.bentonian.framework.math.MathUtil.midPt;

import java.util.List;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.GLVertexData;
import com.bentonian.framework.ui.Vertex;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;



/*      010----------110          --------------          2------------6
 *     /|           /|           /|           /|         /|           /|
 *    / |          / |          / |   2      / |        / |          / |
 *   /  |         /  |         /  |     5   /  |       /  |         /  |
 *  011-----------111|        |-------------|  |      3-------------7  |
 *  |   |         |  |        | 1 |         |4 |      |   |         |  |
 *  |   000-------|--100      |   |---------|--|      |   0---------|--4
 *  |  /          |  /        |  /  0       |  /      |  /          |  /
 *  | /           | /         | /     3     | /       | /           | /
 *  |/            |/          |/            |/        |/            |/
 *  001-----------101         |-------------/         1-------------5
 *    Vertex codes              Face indices           Vertex indices
 */

public class Octree {
  
  public enum State {
    UNEXAMINED,
    REFINED,
    SCHEDULED_FOR_REFINEMENT,
    SCHEDULED_FOR_MISSED_FACES_CHECK,
    FINISHED
  }

  private static final int[][][] TETRAHEDRA = {
    { {0,0,0}, {0,1,1}, {1,0,1}, {1,1,0} },
    { {0,0,0}, {0,0,1}, {1,0,1}, {0,1,1} },
    { {1,1,0}, {0,1,1}, {1,0,1}, {1,1,1} },
    { {0,0,0}, {0,1,1}, {0,1,0}, {1,1,0} },
    { {0,0,0}, {1,0,0}, {1,0,1}, {1,1,0} },
  };

  private static final int[][][] FACES = {
    { {0,0,1}, {1,0,1}, {1,1,1}, {0,1,1} },
    { {0,1,0}, {0,0,0}, {0,0,1}, {0,1,1} },
    { {0,1,1}, {1,1,1}, {1,1,0}, {0,1,0} },
    { {0,0,1}, {1,0,1}, {1,0,0}, {0,0,0} },
    { {1,0,1}, {1,0,0}, {1,1,0}, {1,1,1} },
    { {1,0,0}, {0,0,0}, {0,1,0}, {1,1,0} },
  };

  private static final int[][][] EDGES = {
    { {0,0,0}, {1,0,0} },
    { {1,0,0}, {1,1,0} },
    { {1,1,0}, {0,1,0} },
    { {0,1,0}, {0,0,0} },
    { {0,0,1}, {1,0,1} },
    { {1,0,1}, {1,1,1} },
    { {1,1,1}, {0,1,1} },
    { {0,1,1}, {0,0,1} },
    { {0,0,0}, {0,0,1} },
    { {1,0,0}, {1,0,1} },
    { {1,1,0}, {1,1,1} },
    { {0,1,0}, {0,1,1} },
  };

  private int level;
  private Sample[][][] corners;
  private Set<M3d> targets;
  private Octree parent;
  private Octree[][][] children;
  private ImplicitSurface surface;
  private List<Triangle> polygonList;
  private boolean isOdd;
  private State state = State.UNEXAMINED;

  Octree(ImplicitSurface surface, Octree parent, int level, Sample[][][] coords, 
      int x, int y, int z, Set<M3d> possibleTargets) {
    this.isOdd = (((x+y+z)&1) != 0);
    this.surface = surface;
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

    for (M3d pt : possibleTargets) {
      if (encloses(pt)) {
        addTarget(pt);
      }
    }
  }

  private void polygonalize(boolean isOdd) {
    for (int i = 0; i < TETRAHEDRA.length; i++) {
      int crossings = 0;
      OctreeEdgeInterpolationData[] arr = { null, null, null, null };
      int[][] tet = TETRAHEDRA[i];

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
          arr[0].getNormalDir());
      addPolyWithOrientation(
          b.getInterpolatedCrossing(),
          c.getInterpolatedCrossing(),
          d.getInterpolatedCrossing(),
          arr[0].getNormalDir());
    } else if (crossings == 3) {
      addPolyWithOrientation(
          arr[0].getInterpolatedCrossing(),
          arr[1].getInterpolatedCrossing(),
          arr[2].getInterpolatedCrossing(),
          arr[0].getNormalDir());
    } else if (crossings > 0) {
      System.err.println("Unexpected numCrossings: " + crossings);
    }
  }

  private OctreeEdgeInterpolationData findEdge(Sample a, Sample b) {
    return ((a.force > surface.getCutoff()) != (b.force > surface.getCutoff()))
        ? new OctreeEdgeInterpolationData(a, b, surface.getCutoff())
        : null;
  }

  private Triangle addPolyWithOrientation(Vertex a, Vertex b, Vertex c, M3d normal) {
    M3d n = c.minus(b).cross(a.minus(b)).normalized();

    if (n.dot(normal) > 0) {
      return addPoly(a, b, c, n);
    } else {
      return addPoly(a, c, b, n.neg());
    }
  }

  private Triangle addPoly(Vertex a, Vertex b, Vertex c, M3d normal) {
    Triangle poly = new Triangle(a, b, c, normal);
    polygonList.add(poly);
    return poly;
  }

  boolean hasPolygons() {
    return !getPolygonList().isEmpty();
  }

  void renderEdges(GLVertexData edgesVao) {
    for (Triangle poly : getPolygonList()) {
      edgesVao.vertex(poly.a);
      edgesVao.vertex(poly.b);
      edgesVao.vertex(poly.c);
    }
  }

  void renderFaces(GLVertexData surfaceVao, boolean showNormals, boolean blendColors) {
    for (Triangle poly : getPolygonList()) {
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

  List<Triangle> getPolygonList() {
    if (polygonList == null) {
      polygonList = Lists.newArrayList();
      polygonalize(isOdd);
    }
    return polygonList;
  }

  void addTarget(M3d pt) {
    targets.add(pt);
  }

  boolean encloses(M3d pt) {
    return pt.getX() >= corners[0][0][0].getX() && pt.getX() <= corners[1][1][1].getX()
        && pt.getY() >= corners[0][0][0].getY() && pt.getY() <= corners[1][1][1].getY()
        && pt.getZ() >= corners[0][0][0].getZ() && pt.getZ() <= corners[1][1][1].getZ();
  }

  boolean isInteresting() {
    if (!targets.isEmpty()) {
      return true;
    } else {
      boolean first = surface.isHot(corners[0][0][0]);

      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          for (int k = 0; k < 2; k++) {
            if (surface.isHot(corners[i][j][k]) != first) {
              return true;
            }
          }
        }
      }
      return false;
    }
  }

  boolean hasInterestingFace(int faceIndex) {
    int[][] faceCorners = FACES[faceIndex];
    boolean first = surface.isHot(corners[faceCorners[0][0]][faceCorners[0][1]][faceCorners[0][2]]);

    for (int i = 1; i < 4; i++) {
      if (surface.isHot(corners[faceCorners[i][0]][faceCorners[i][1]][faceCorners[i][2]]) != first) {
        return true;
      }
    }
    return false;
  }

  M3d getPointJustBeyondFace(int faceIndex) {
    M3d faceCenter = new M3d();
    M3d center = midPt(corners[0][0][0], corners[1][1][1]);

    for (int i = 0; i < 4; i++) {
      faceCenter = faceCenter.plus(getCorner(FACES[faceIndex][i]));
    }
    faceCenter = faceCenter.times(0.25);
    return center.plus(faceCenter.minus(center).times(1.1));
  }

  private Sample getCorner(int[] index) {
    return corners[index[0]][index[1]][index[2]];
  }

  private Sample getNewSample(M3d v) {
    return surface.sumForces(v);
  }

  private Sample[][][] makeSubCorners() {
    Sample[][][] subCorners = new Sample[3][3][3];

    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 2; k++) {
          subCorners[i*2][j*2][k*2] = corners[i][j][k];
        }
      }
    }

    for (int edge = 0; edge < 12; edge++) {
      int[][] endPoints = EDGES[edge];
      int i = endPoints[0][0] + endPoints[1][0];
      int j = endPoints[0][1] + endPoints[1][1];
      int k = endPoints[0][2] + endPoints[1][2];
      subCorners[i][j][k] = getNewSample(midPt(getCorner(endPoints[0]), getCorner(endPoints[1])));
    }

    for (int face = 0; face < 6; face++) {
      int[][] faceIndices = FACES[face];
      int i = (faceIndices[0][0] + faceIndices[1][0] + faceIndices[2][0] + faceIndices[3][0]) / 2;
      int j = (faceIndices[0][1] + faceIndices[1][1] + faceIndices[2][1] + faceIndices[3][1]) / 2;
      int k = (faceIndices[0][2] + faceIndices[1][2] + faceIndices[2][2] + faceIndices[3][2]) / 2;
      subCorners[i][j][k] = getNewSample(midPt(getCorner(faceIndices[0]), getCorner(faceIndices[2])));
    }

    subCorners[1][1][1] = getNewSample(midPt(corners[0][0][0], corners[1][1][1]));

    return subCorners;
  }

  int getLevel() {
    return level;
  }

  M3d getMin() {
    return corners[0][0][0];
  }

  M3d getMax() {
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

  M3d[][][] getCorners() {
    return corners;
  }

  Octree[][][] getChildOctrees() {
    return children;
  }

  void refine() {
    if (children == null) {
      Sample[][][] subCorners = makeSubCorners();

      children = new Octree[2][2][2];
      for (int i = 0; i < 2; i++) {
        for (int j = 0; j < 2; j++) {
          for (int k = 0; k < 2; k++) {
            Octree child = new Octree(surface, this, level + 1, subCorners, i, j, k, targets);
            children[i][j][k] = child;
          }
        }
      }
    }
  }
}