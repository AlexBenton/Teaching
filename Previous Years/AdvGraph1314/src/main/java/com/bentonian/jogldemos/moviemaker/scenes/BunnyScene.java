package com.bentonian.jogldemos.moviemaker.scenes;

import static java.lang.Math.min;

import com.bentonian.framework.io.OFFUtil;
import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.mesh.advanced.TexturedMeshPrimitive;
import com.bentonian.framework.mesh.primitive.Circle;
import com.bentonian.framework.mesh.primitive.MeshPrimitive;
import com.bentonian.framework.mesh.textures.VolumetricTexture;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class BunnyScene extends MovieMakerScene {

  public BunnyScene() {
    MeshPrimitive bunny = new MeshPrimitive(OFFUtil.parseFile("bunny.off"));
    bunny.enableRayTracingAccelerator();
    bunny.setTransparency(0.5);
    bunny.setRefractiveIndex(1.05);
    add(bunny);
    add(new Backdrop());
    add(new Circle()
        .translate(new M3d(0, -0.499, 0)));
  }

  @Override
  public int getNumShadowRays() {
    return 16;
  }

  private static class Backdrop extends TexturedMeshPrimitive {
    public Backdrop() {
      super(new Mesh(), VolumetricTexture.WOOD);
      Vertex[][] grid = new Vertex[3][40];
      for (int i = 0; i < 3; i++) {
        for (int j = 0; j < 40; j++) {
          double jp = min(0, j - 20);
          grid[i][j] = new Vertex((i - 1) * 20, 0.1 * jp * jp - 0.5, j - 20 - 5);
        }
      }
      for (int i = 0; i < 3 - 1; i++) {
        for (int j = 10; j < 35 - 1; j++) {
          getMesh().add(new Face(grid[i][j], grid[i][j+1], grid[i+1][j+1], grid[i+1][j]));
        }
      }
      getMesh().computeAllNormals();
      setLightingCoefficients(0.25, 0.25, 0.65, 0.4);
      enableRayTracingAccelerator();
    }

    @Override
    protected Material getMaterial(M3d pt) {
      return super.getMaterial(pt.times(0.2));
    }
  }
}
