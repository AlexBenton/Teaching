package com.bentonian.framework.texture;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import com.bentonian.framework.math.M3d;

public class VolumetricTexture extends BufferedProceduralImageTexture {

  public static final Texture WOOD = new VolumetricTexture() {
    private final M3d OLD_WOOD = new M3d(72, 38, 11).times(1.0 / 255.0);
    private final M3d NEW_WOOD = new M3d(175, 88, 45).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double TEXTURE_FREQUENCY = 2;
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 0;//0.5;

    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      double f = TEXTURE_FREQUENCY * (pt.getX() * pt.getX() + pt.getZ() * pt.getZ() - abs(pt.getY() + 5) / 5);
      double n = NOISE_AMPLITUDE * NOISE.get(pt.times(NOISE_FREQUENCY));
      f = f + n;
      f = f - Math.floor(f);
      f = Math.pow(f, 0.5);
      return OLD_WOOD.plus(NEW_WOOD.minus(OLD_WOOD).times(f));
    }
  };
  
  public static final Texture PERLIN_FRACTAL = new VolumetricTexture() {
    private final M3d DARK = new M3d(50, 50, 50).times(1.0 / 255.0);
    private final M3d LIGHT = new M3d(250, 250, 250).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 0.5;

    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      double n = NOISE_AMPLITUDE * (
          NOISE.get(pt.times(NOISE_FREQUENCY))
          + 0.5 * NOISE.get(pt.times(NOISE_FREQUENCY).times(2))
          + 0.25 * NOISE.get(pt.times(NOISE_FREQUENCY).times(4))
          + 0.125 * NOISE.get(pt.times(NOISE_FREQUENCY).times(8))
          + 0.0625 * NOISE.get(pt.times(NOISE_FREQUENCY).times(16)));
      n = min(max(n, 0), 1);
      return DARK.plus(LIGHT.minus(DARK).times(n));
    }
  };
  
  public static final Texture PERLIN_FRACTAL_FIRE = new VolumetricTexture() {
    private final M3d DARK = new M3d(60, 10, 10).times(1.0 / 255.0);
    private final M3d LIGHT = new M3d(218, 165, 32).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 0.5;

    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      double n = NOISE_AMPLITUDE * (
          NOISE.get(pt.times(NOISE_FREQUENCY))
          + 0.5 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(2)))
          + 0.25 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(4)))
          + 0.125 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(8)))
          + 0.0625 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(16))));
      n = min(max(n, 0), 1);
      return DARK.plus(LIGHT.minus(DARK).times(n));
    }
  }; 
  
  public static final Texture MARBLE = new VolumetricTexture() {
    private final M3d DARK = new M3d(150, 150, 150).times(1.0 / 255.0);
    private final M3d LIGHT = new M3d(250, 250, 250).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double TEXTURE_FREQUENCY = 6;
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 2;

    @Override
    public M3d getColor(IsTextured target, M3d pt) {
      double f = Math.atan2(pt.getX(), pt.getZ()) * TEXTURE_FREQUENCY;
      double n = NOISE_AMPLITUDE * (
          NOISE.get(pt.times(NOISE_FREQUENCY))
          + 0.5 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(2)))
          + 0.25 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(4)))
          + 0.125 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(8)))
          + 0.0625 * abs(NOISE.get(pt.times(NOISE_FREQUENCY).times(16))));
      f = Math.sin(f + n);
      return DARK.plus(LIGHT.minus(DARK).times(f));
    }
  };
}
