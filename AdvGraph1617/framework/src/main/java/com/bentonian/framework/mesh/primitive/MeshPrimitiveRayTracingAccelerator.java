package com.bentonian.framework.mesh.primitive;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import com.bentonian.framework.math.LineSegment;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class MeshPrimitiveRayTracingAccelerator {

  private static final int DIM = 50;

  private static class GridCell extends HashSet<MeshFace> { }

  private static class GridCellCoord { 
    int u, v, w;
    public GridCellCoord(int u, int v, int w) {
      this.u = u;
      this.v = v;
      this.w = w;
    }
    @Override
    public int hashCode() {
      return 31 * (31 * (31 * u) + v) + w;
    }
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof GridCellCoord) ? ((GridCellCoord) obj).hashCode() == hashCode() : false;
    }
  }

  private final M3d least, most;
  private final GridCell[][][] grid;

  public MeshPrimitiveRayTracingAccelerator(Mesh mesh) {
    this.grid = new GridCell[DIM][DIM][DIM];
    this.least = new M3d();
    this.most = new M3d();

    mesh.getBounds(least, most);
    most.set(most.plus(new M3d(MathConstants.EPSILON, MathConstants.EPSILON, MathConstants.EPSILON)));

    for (MeshFace face : mesh) {
      for (int i = 0; i < face.size() - 2; i++) {
        M3d A = face.get(0);
        M3d B = face.get(i + 1);
        M3d C = face.get(i + 2);
        M3d triMin = new M3d();
        M3d triMax = new M3d();
        getTriangleBoundingBox(A, B, C, triMin, triMax);
        GridCellCoord min = getCellCoord(triMin);
        GridCellCoord max = getCellCoord(triMax);
        for (int u = min.u; u <= max.u; u++) {
          for (int v = min.v; v <= max.v; v++) {
            for (int w = min.w; w <= max.w; w++) {
              if (triangleOverlapsBox(u, v, w, A, B, C)) {
                if (grid[u][v][w] == null) {
                  grid[u][v][w] = new GridCell();
                }
                grid[u][v][w].add(face);
              }
            }
          }
        }
      }
    }
  }

  public Set<MeshFace> getFacesAlongRay(Ray ray) {
    Set<MeshFace> candidates = Sets.newHashSet();
    Set<GridCellCoord> visited = Sets.newHashSet();
    LinkedList<GridCellCoord> coords = Lists.newLinkedList();
    M3d pt = ray.at(ray.intersectsCubePrecisely(least, most) + MathConstants.EPSILON);

    GridCellCoord coord = getCellCoord(pt);
    coords.add(coord);
    visited.add(coord);
    while (!coords.isEmpty()) {
      coord = coords.pop();
      if ((coord.u >= 0) && (coord.v >= 0) && (coord.w >= 0) 
          && (coord.u < DIM) && (coord.v < DIM) && (coord.w < DIM)
          && (grid[coord.u][coord.v][coord.w] != null)) {
        candidates.addAll(grid[coord.u][coord.v][coord.w]);
      }

      // Find the set of next neighbors.
      // Yes I *know* I could do this vastly more efficiently.
      for (int i = coord.u - 1; i <= coord.u + 1; i++) {
        for (int j = coord.v - 1; j <= coord.v + 1; j++) {
          for (int k = coord.w - 1; k <= coord.w + 1; k++) {
            if ((i >= 0) && (j >= 0) && (k >= 0) && (i < DIM) && (j < DIM) && (k < DIM)) {
              GridCellCoord next = new GridCellCoord(i, j, k);
              if (!visited.contains(next) && rayHitsGridCell(next, ray)) {
                coords.add(next);
                visited.add(next);
              }
            }
          }
        }
      }
    }
    return candidates;
  }

  public boolean isHitByRay(Ray ray) {
    return ray.intersectsCube(least, most);
  }

  public boolean triangleOverlapsBox(int i, int j, int k, M3d A, M3d B, M3d C) {
    M3d boxMin = least.plus(new M3d(
        i * (most.getX() - least.getX()) / DIM,
        j * (most.getY() - least.getY()) / DIM,
        k * (most.getZ() - least.getZ()) / DIM));
    M3d boxMax = boxMin.plus(new M3d(
        (most.getX() - least.getX()) / DIM,
        (most.getY() - least.getY()) / DIM,
        (most.getZ() - least.getZ()) / DIM));
    M3d[] verts = new M3d[]{ A, B, C };

    // Test vertices directly
    for (M3d v : verts) {
      if (boxMin.lessThan(v) && v.lessThan(boxMax)) {
        return true;
      }
    }

    // Compare bboxes
    M3d triMin = new M3d();
    M3d triMax = new M3d();
    getTriangleBoundingBox(A, B, C, triMin, triMax);
    if ((boxMin.getX() > triMax.getX())
        || (boxMin.getY() > triMax.getY())
        || (boxMin.getZ() > triMax.getZ())
        || (boxMax.getX() < triMin.getX())
        || (boxMax.getY() < triMin.getY())
        || (boxMax.getZ() < triMin.getZ())) {
      return false;
    }

    // Test triangle edges
    for (int c = 0; c < 3; c++) {
      if (new LineSegment(verts[c], verts[(c + 1) % 3]).intersectsCube(boxMin, boxMax)) {
        return true;
      }
    }

    // Test triangle cuts through cube by testing cube diagonals
    M3d N = B.minus(A).cross(C.minus(A));
    M3d diff = boxMax.minus(boxMin);
    for (int u = 0; u <= 1; u++) {
      for (int v = 0; v <= 1; v++) {
        LineSegment cubeDiagonal = new LineSegment(
            boxMin.plus(new M3d(diff.getX() * u, 0, diff.getZ() * v)),
            boxMin.plus(new M3d(diff.getX() * (1 - u), diff.getY(), diff.getZ() * (1 - v))));
        if (cubeDiagonal.intersectsTriangle(A, B, C, N)) {
          return true;
        }
      }
    }

    // No dice!
    return false;
  }
  
  private boolean rayHitsGridCell(GridCellCoord coord, Ray ray) {
    M3d diff = most.minus(least).times(1.0 / DIM);
    M3d min = least.plus(new M3d(
        coord.u * diff.getX(),
        coord.v * diff.getY(),
        coord.w * diff.getZ()));
    M3d max = least.plus(new M3d(
        (coord.u + 1) * diff.getX(),
        (coord.v + 1) * diff.getY(),
        (coord.w + 1) * diff.getZ()));
    return ray.intersectsCube(min, max);
  }

  private GridCellCoord getCellCoord(M3d pt) {
    return new GridCellCoord(
        getCellIndex(pt, 0),
        getCellIndex(pt, 1),
        getCellIndex(pt, 2));
  }

  private int getCellIndex(M3d pt, int axisIndex) {
    return (int) ((pt.get(axisIndex) - least.get(axisIndex)) * DIM
        / (most.get(axisIndex) - least.get(axisIndex)));
  }

  private void getTriangleBoundingBox(M3d A, M3d B, M3d C, M3d triMin, M3d triMax) {
    triMin.set(
        min(A.getX(), min(B.getX(), C.getX())),
        min(A.getY(), min(B.getY(), C.getY())),
        min(A.getZ(), min(B.getZ(), C.getZ())));
    triMax.set(
        max(A.getX(), max(B.getX(), C.getX())),
        max(A.getY(), max(B.getY(), C.getY())),
        max(A.getZ(), max(B.getZ(), C.getZ())));
  }
}
