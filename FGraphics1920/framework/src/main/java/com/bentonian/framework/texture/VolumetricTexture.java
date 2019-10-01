package com.bentonian.framework.texture;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import com.bentonian.framework.math.Vec3;

public class VolumetricTexture extends BufferedProceduralImageTexture {

  public static final Texture WOOD = new VolumetricTexture() {
    private final Vec3 OLD_WOOD = new Vec3(72, 38, 11).times(1.0 / 255.0);
    private final Vec3 NEW_WOOD = new Vec3(175, 88, 45).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double TEXTURE_FREQUENCY = 2;
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 0;//0.5;

    @Override
    public Vec3 getColor(IsTextured target, Vec3 pt) {
      double f = TEXTURE_FREQUENCY * (pt.getX() * pt.getX() + pt.getZ() * pt.getZ() - abs(pt.getY() + 5) / 5);
      double n = NOISE_AMPLITUDE * NOISE.get(pt.times(NOISE_FREQUENCY));
      f = f + n;
      f = f - Math.floor(f);
      f = Math.pow(f, 0.5);
      return OLD_WOOD.plus(NEW_WOOD.minus(OLD_WOOD).times(f));
    }
  };
  
  public static final Texture PERLIN_FRACTAL = new VolumetricTexture() {
    private final Vec3 DARK = new Vec3(50, 50, 50).times(1.0 / 255.0);
    private final Vec3 LIGHT = new Vec3(250, 250, 250).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 0.5;

    @Override
    public Vec3 getColor(IsTextured target, Vec3 pt) {
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
    private final Vec3 DARK = new Vec3(60, 10, 10).times(1.0 / 255.0);
    private final Vec3 LIGHT = new Vec3(218, 165, 32).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 0.5;

    @Override
    public Vec3 getColor(IsTextured target, Vec3 pt) {
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
    private final Vec3 DARK = new Vec3(150, 150, 150).times(1.0 / 255.0);
    private final Vec3 LIGHT = new Vec3(250, 250, 250).times(1.0 / 255.0);
    private final PerlinNoise NOISE = new PerlinNoise();
    private final double TEXTURE_FREQUENCY = 6;
    private final double NOISE_FREQUENCY = 2;
    private final double NOISE_AMPLITUDE = 2;

    @Override
    public Vec3 getColor(IsTextured target, Vec3 pt) {
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
