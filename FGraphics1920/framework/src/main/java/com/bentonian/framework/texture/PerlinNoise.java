package com.bentonian.framework.texture;

import static java.lang.Math.floor;
import static java.lang.Math.random;

import java.util.Map;

import com.bentonian.framework.math.Vec3;
import com.google.common.collect.Maps;

public class PerlinNoise {
  
  private final Map<Vec3 /* grid point */, Vec3 /* direction */> cache = Maps.newHashMap();
  
  public double get(Vec3 pt /* point in space */) {
    Vec3 base = new Vec3(floor(pt.getX()), floor(pt.getY()), floor(pt.getZ()));
    double[][][] noise = new double[2][2][2];
    double[][] interpolantEdge = new double[2][2];
    double[] interpolantFace = new double[2];
    for (int x = 0; x <= 1; x++) {
      for (int y = 0; y <= 1; y++) {
        for (int z = 0; z <= 1; z++) {
          Vec3 gridPt = base.plus(new Vec3(x, y, z));
          Vec3 direction = lookup(gridPt);
          noise[x][y][z] = direction.dot(pt.minus(gridPt));
        }
        interpolantEdge[x][y] = easeInterpolate(noise[x][y][0], noise[x][y][1], pt.getZ() - base.getZ());
      }
      interpolantFace[x] = easeInterpolate(interpolantEdge[x][0], interpolantEdge[x][1], pt.getY() - base.getY());
    }
    return easeInterpolate(interpolantFace[0], interpolantFace[1], pt.getX() - base.getX());
  }

  private double easeInterpolate(double r0, double r1, double t) {
    return r0 + (3 * t * t - 2 * t * t * t) * (r1 - r0);
  }

  private Vec3 lookup(Vec3 gridPt) {
    Vec3 direction = cache.get(gridPt);
    if (direction == null) {
      direction = new Vec3(random() * 2 - 1, random() * 2 - 1, random() * 2 - 1).normalized();
      cache.put(gridPt, direction);
    }
    return direction;
  }
}