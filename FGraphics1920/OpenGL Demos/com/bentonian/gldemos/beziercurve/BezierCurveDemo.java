package com.bentonian.gldemos.beziercurve;

import static com.bentonian.framework.ui.ShaderUtil.loadShader;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.ShaderAutoloader;

public class BezierCurveDemo extends DemoApp {

  private static final int NUM_SEEDS = 4;
  private static final int ZOOM = 6;
  
  private final Square square;
  private Camera frozenCamera;
  private long lastTick;
  private boolean paused = false;
  
  private Vec3[] seedCoords = new Vec3[NUM_SEEDS];
  private FloatBuffer seeds;
  private int draggingSeed = -1;
  private long elapsed;
  private ShaderAutoloader loader;

  protected BezierCurveDemo() {
    super("Bezier Curve Demo");
    this.square = new Square();
    this.square.setHasTexture(true);
    this.paused = false;
    this.lastTick = System.currentTimeMillis();

    for (int i = 0; i < 4; i++) {
      seedCoords[i] = new Vec3(i - 1.5, 0, 0);
    }
    updateSeedsBuffer();

    setCameraDistance(2.15);
    this.frozenCamera = new Camera(getCamera());
    animateCameraToPosition(new Vec3(20, 10, 20));

    String root = BezierCurveDemo.class.getPackage().getName().replace(".", "/") + "/";
    this.loader = new ShaderAutoloader(
        new String[] { root },
        () -> loadShader(GL20.GL_VERTEX_SHADER, root + "shader.vsh"),
        () -> loadShader(GL20.GL_FRAGMENT_SHADER, root + "shader.fsh"),
        () -> exitRequested,
        (p) -> useProgram(p),
        (e) -> System.err.println(e)
    );
  }

  @Override
  protected Camera getCameraForModelview() {
    return frozenCamera;
  }

  static class Coord {
    final int i;
    final int j;
    Coord(int i, int j) {this.i=i;this.j=j;}
    @Override
    public int hashCode() {
      return 31 * (31 + i) + j;
    }
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Coord)
          && ((Coord) obj).i == i
          && ((Coord) obj).j == j;
    }
    Coord plus(Coord c) {
      return new Coord(i + c.i, j + c.j);
    }
  }
  
  static final Coord DC[] = new Coord[] { 
      new Coord(0, 1),
      new Coord(1, 0),
      new Coord(0, -1),
      new Coord(-1, 0)
  };
  
  static final Coord CORNERS[] = new Coord[] { 
      new Coord(0, 1),
      new Coord(1, 1),
      new Coord(1, 0),
      new Coord(0, 0)
  };

  @Override
  public void preDraw() {
    loader.preDraw();
    super.preDraw();
  }

  @Override
  public void draw() {
    long now = System.currentTimeMillis();

    if (!paused) {
      elapsed += now - lastTick;
    }
    lastTick = now;
    updateUniformVec2("iResolution", (float) getWidth(), (float) getHeight());
    updateUniformFloat("iGlobalTime", elapsed / 1000.0f);
    updateUniform3fv("seeds", seeds);
    square.render(this);
  }

  @Override
  protected void onResized(int width, int height) {
    super.onResized(width, height);
    square.setIdentity();
    square.scale(new Vec3(width / (float) height, 1, 1));
    if (getProgram() != -1) {
      updateUniformVec2("iResolution", (float) width, (float) height);
    }
  }
  
  Vec3 screenToGridSpace(int x, int y) {
    return new Vec3(
        ZOOM * ((x / (float) getWidth()) * 2 - 1) * getWidth() / getHeight(), 
        ZOOM * (((getHeight() - y) / (float) getHeight()) * 2 - 1), 
        0);
  }
  
  int findSeedOnScreen(int x, int y) {
    Vec3 pos = screenToGridSpace(x, y);
    for (int i = 0; i < NUM_SEEDS; i++) {
      if (pos.minus(seedCoords[i]).length() < 0.2) {
        return i;
      }
    }
    return -1;
  }
  
  void updateSeedsBuffer() {
    seeds = BufferUtils.createFloatBuffer(3 * NUM_SEEDS);
    for (int n = 0; n < NUM_SEEDS; n++) {
      seeds.put((float) seedCoords[n].getX());
      seeds.put((float) seedCoords[n].getY());
      seeds.put((float) seedCoords[n].getZ());
    }
    seeds.flip();
  }

  @Override
  protected void onMouseDown(int x, int y) {
    super.onMouseDown(x, y);
    draggingSeed = findSeedOnScreen(x, y);
  }
  
  @Override
  protected void onMouseDrag(int x, int y) {
    if (draggingSeed != -1) {
      seedCoords[draggingSeed] = screenToGridSpace(x, y);
      updateSeedsBuffer();
    }
  }
    
  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case GLFW.GLFW_KEY_A:
      elapsed = 0;
      break;
    case GLFW.GLFW_KEY_J:
      elapsed -= 250;
      break;
    case GLFW.GLFW_KEY_K:
      elapsed += 250;
      break;
    case GLFW.GLFW_KEY_SPACE:
      paused = !paused;
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new BezierCurveDemo().run();
  }
}
