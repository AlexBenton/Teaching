package com.bentonian.gldemos.shaders;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M3d;

public class VoronoiRenderer extends ShaderRenderer {
  
  private static final int NUM_SEEDS = 16;

  private M3d[] coords = new M3d[NUM_SEEDS];
  private FloatBuffer seeds = null;
  private int numSeeds = 0;

  public VoronoiRenderer() {
    super("voronoi.vsh", "voronoi.fsh");
  }

  @Override
  public void render(ShaderDemo shaderDemo, ShaderModel model) {
    M3d pos = shaderDemo.getPongPoint();
    if (pos != null) {
      int i;
      for (i = 0; i < numSeeds; i++) {
        if (pos.equals(coords[i])) {
          break;
        }
      }
      if (i == numSeeds) {
        if (numSeeds == NUM_SEEDS) {
          numSeeds = 0;
        }
        addSeed(pos);
      }
    }

    if (seeds != null) {
      GL20.glUniform3fv(GL20.glGetUniformLocation(shaderProgram, "seeds"), seeds);
    }
    super.render(shaderDemo, model);
  }
  
  private void addSeed(M3d pos) {
    coords[numSeeds++] = pos;
    seeds = BufferUtils.createFloatBuffer(3 * NUM_SEEDS);
    for (int i = 0; i < numSeeds; i++) {
      seeds.put((float) coords[i].getX());
      seeds.put((float) coords[i].getY());
      seeds.put((float) coords[i].getZ());
    }
    for (int i = numSeeds; i < NUM_SEEDS; i++) {
      seeds.put(0.0f);
      seeds.put(0.0f);
      seeds.put(0.0f);
    }
    seeds.flip();
    System.out.println("Num seeds: " + numSeeds + ".  Latest seed: [" + pos + "]");
  }
}
