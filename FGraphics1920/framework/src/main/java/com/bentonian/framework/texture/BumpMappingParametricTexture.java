package com.bentonian.framework.texture;

import static com.bentonian.framework.material.Colors.RED;
import static com.bentonian.framework.material.Colors.WHITE;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.floor;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.math.M4x4;

public class BumpMappingParametricTexture extends ParametricTexture {

  public static final Texture BRICK = new BumpMappingParametricTexture() {
    @Override
    public Vec3 getColor(IsTextured target, Vec3 pt) {
      TexCoord tc = target.getTextureCoord(pt);
      Double dx = distToEdge(tc.u);
      Double dy = distToEdge(tc.v);
      return ((dx != null) || (dy != null)) ? WHITE : RED;
    }

    @Override
    public Vec3 getNormal(IsTextured target, Vec3 pt, Vec3 normal) {
      TexCoord tc = target.getTextureCoord(pt);
      Vec3 uBasis = target.getUBasis(pt);
      Double dx = distToEdge(tc.u);
      Double dy = distToEdge(tc.v);
      if ((dx != null) && ((dy == null) || (abs(dx) < abs(dy)))) {
        normal = M4x4.rotationMatrix(uBasis, tcToAngle(dx)).times(normal);
      } else if (dy != null) {
        Vec3 vBasis = uBasis.cross(normal).normalized();
        normal = M4x4.rotationMatrix(vBasis, tcToAngle(dy)).times(normal);
      }

      return normal;
    }

    private Double distToEdge(double tc) {
      double tile = 10 * tc - floor(10 * tc);
      if (tile < 0.05) {
        return -tile;
      } else if (tile > 0.95) {
        return (1 - tile);
      } else {
        return null;
      }
    }

    private double tcToAngle(double distToEdge) {
      return -(PI_OVER_TWO - (acos(distToEdge * 20) / 2));
    }
  };
  
  public static final Texture PITTED = new BumpMappingParametricTexture() {
    @Override
    public Vec3 getColor(IsTextured target, Vec3 pt) {
      TexCoord tc = target.getTextureCoord(pt);
      return (noisyNormal(tc).getY() < 1) ? WHITE : RED;
    }

    @Override
    public Vec3 getNormal(IsTextured target, Vec3 pt, Vec3 normal) {
      TexCoord tc = target.getTextureCoord(pt);
      Vec3 N = noisyNormal(tc);
      Vec3 axis = normal.cross(Y_AXIS);
      double phi = acos(normal.dot(Y_AXIS));
      M4x4 rotation = M4x4.rotationMatrix(axis, phi);
      return rotation.times(N);
    }
    
    private Vec3 noisyNormal(TexCoord tc) {
      final double NOISE_SCALE = 50;
      double L = declamp(NOISE.get(new Vec3(tc.u - 0.001, 0, tc.v).times(NOISE_SCALE)));
      double R = declamp(NOISE.get(new Vec3(tc.u + 0.001, 0, tc.v).times(NOISE_SCALE)));
      double B = declamp(NOISE.get(new Vec3(tc.u, 0, tc.v - 0.001).times(NOISE_SCALE)));
      double T = declamp(NOISE.get(new Vec3(tc.u, 0, tc.v + 0.001).times(NOISE_SCALE)));
      return new Vec3(0, T-B, 1).cross(new Vec3(1, R-L, 0)).normalized();
    }
    
    private double declamp(double d) {
      final double threshold = 0.25;
      d = abs(d);
      return (d < threshold) ? 0 : d;
    }
  }
  ;

  private static final Vec3 Y_AXIS = new Vec3(0, 1, 0);
  private static final double PI_OVER_TWO = (PI / 2);
  private static final PerlinNoise NOISE = new PerlinNoise();
}
