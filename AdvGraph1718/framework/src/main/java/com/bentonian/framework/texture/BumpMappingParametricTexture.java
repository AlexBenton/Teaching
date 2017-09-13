package com.bentonian.framework.texture;

import static com.bentonian.framework.material.Colors.RED;
import static com.bentonian.framework.material.Colors.WHITE;
import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.floor;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;

public class BumpMappingParametricTexture extends ParametricTexture {

  public static final Texture BRICK = new BumpMappingParametricTexture() {
    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      TexCoord tc = target.getTextureCoord(pt);
      Double dx = distToEdge(tc.u);
      Double dy = distToEdge(tc.v);
      return ((dx != null) || (dy != null)) ? WHITE : RED;
    }

    @Override
    public M3d getNormal(IsTextured target, M3d pt, M3d normal) {
      TexCoord tc = target.getTextureCoord(pt);
      M3d uBasis = target.getUBasis(pt);
      Double dx = distToEdge(tc.u);
      Double dy = distToEdge(tc.v);
      if ((dx != null) && ((dy == null) || (abs(dx) < abs(dy)))) {
        normal = M4x4.rotationMatrix(uBasis, tcToAngle(dx)).times(normal);
      } else if (dy != null) {
        M3d vBasis = uBasis.cross(normal).normalized();
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
    public M3d getColor(IsTextured target, M3d pt) {
      TexCoord tc = target.getTextureCoord(pt);
      return (noisyNormal(tc).getY() < 1) ? WHITE : RED;
    }

    @Override
    public M3d getNormal(IsTextured target, M3d pt, M3d normal) {
      TexCoord tc = target.getTextureCoord(pt);
      M3d N = noisyNormal(tc);
      M3d axis = normal.cross(Y_AXIS);
      double phi = acos(normal.dot(Y_AXIS));
      M4x4 rotation = M4x4.rotationMatrix(axis, phi);
      return rotation.times(N);
    }
    
    private M3d noisyNormal(TexCoord tc) {
      final double NOISE_SCALE = 50;
      double L = declamp(NOISE.get(new M3d(tc.u - 0.001, 0, tc.v).times(NOISE_SCALE)));
      double R = declamp(NOISE.get(new M3d(tc.u + 0.001, 0, tc.v).times(NOISE_SCALE)));
      double B = declamp(NOISE.get(new M3d(tc.u, 0, tc.v - 0.001).times(NOISE_SCALE)));
      double T = declamp(NOISE.get(new M3d(tc.u, 0, tc.v + 0.001).times(NOISE_SCALE)));
      return new M3d(0, T-B, 1).cross(new M3d(1, R-L, 0)).normalized();
    }
    
    private double declamp(double d) {
      final double threshold = 0.25;
      d = abs(d);
      return (d < threshold) ? 0 : d;
    }
  }
  ;

  private static final M3d Y_AXIS = new M3d(0, 1, 0);
  private static final double PI_OVER_TWO = (PI / 2);
  private static final PerlinNoise NOISE = new PerlinNoise();
}
