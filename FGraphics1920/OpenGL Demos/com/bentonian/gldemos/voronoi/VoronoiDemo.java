package com.bentonian.gldemos.voronoi;

import static com.bentonian.framework.ui.ShaderUtil.loadShader;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.ShaderAutoloader;

public class VoronoiDemo extends DemoApp {

  static private final double CAMERA_DISTANCE = 5.0;
  private static final int NUM_SEEDS = 16;

  private Model model;
  private boolean spin = false;
  private ShaderAutoloader loader;

  private Vec3[] seedCoords = new Vec3[NUM_SEEDS];
  private FloatBuffer seeds = null;
  private int numSeeds = 0;
  private long startTime;
  
  public VoronoiDemo() {
    super("Voronoi Cells demo");
    this.model = Model.CUBE;
    setCameraDistance(CAMERA_DISTANCE);
    startTime = System.currentTimeMillis();

    updateSeedsBuffer();
    String root = VoronoiDemo.class.getPackage().getName().replace(".", "/") + "/";
    this.loader = new ShaderAutoloader(
        new String[] { root },
        () -> loadShader(GL20.GL_VERTEX_SHADER, root + "voronoi.vsh"),
        () -> loadShader(GL20.GL_FRAGMENT_SHADER, root + "voronoi.fsh"),
        () -> exitRequested,
        (p) -> useProgram(p),
        (e) -> System.err.println(e));
  }

  @Override
  public String getTitle() {
    return "Voronoi Cells Demo";
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case GLFW.GLFW_KEY_SPACE:
      spin = !spin;
      break;
    case GLFW.GLFW_KEY_0:
    {
      Vec3 pt = new Vec3(1, 1, 1).normalized().times(CAMERA_DISTANCE);
      pt = M4x4.rotationMatrix(new Vec3(0, 1, 0), 0.15).times(pt);
      pt = M4x4.rotationMatrix(getCamera().getLocalToParent().extract3x3().times(new Vec3(1, 0, 0)), -0.15).times(pt);
      animateCameraToPosition(pt);
      break;
    }
    case GLFW.GLFW_KEY_LEFT_BRACKET:
      model = model.prev();
      model.dispose();
      break;
    case GLFW.GLFW_KEY_RIGHT_BRACKET:
      model = model.next();
      model.dispose();
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  void updateSeedsBuffer() {
    seeds = BufferUtils.createFloatBuffer(3 * NUM_SEEDS);
    for (int n = 0; n < numSeeds; n++) {
      seeds.put((float) seedCoords[n].getX());
      seeds.put((float) seedCoords[n].getY());
      seeds.put((float) seedCoords[n].getZ());
    }
    for (int n = numSeeds; n < NUM_SEEDS; n++) {
      seeds.put(0.0f);
      seeds.put(0.0f);
      seeds.put(0.0f);
    }
    seeds.flip();
  }
  
  @Override
  protected void onMouseClick(int x, int y) {
    super.onMouseClick(x, y);

    Vec3 pt = pickPoint(model.getGeometry(), x, y);
    
    int n;
    for (n = 0; n < numSeeds; n++) {
      if (seedCoords[n].minus(pt).length() < 0.01) {
        break;
      }
    }
    if (n < numSeeds) {
      seedCoords[n] = seedCoords[numSeeds - 1];
      numSeeds--;
    } else if (numSeeds < NUM_SEEDS) {
      seedCoords[numSeeds++] = pt;
    } 
    updateSeedsBuffer();
  }

  @Override
  public void preDraw() {
    loader.preDraw();
    super.preDraw();
  }

  @Override
  protected void draw() {
    updateUniformInt("numSeeds", numSeeds);
    updateUniform3fv("seeds", seeds);    
    updateUniformFloat("iGlobalTime", (System.currentTimeMillis() - startTime) / 1000.0f);
    model.getGeometry().render(this);
  }

  public static void main(String[] args) {
    new VoronoiDemo().run();
  }
}
