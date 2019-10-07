package com.bentonian.gldemos.marchingcubes;

import static com.bentonian.framework.ui.ShaderUtil.loadShader;

import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.io.FileUtil;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.ui.BufferedImageRGBCanvas;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.ShaderAutoloader;

public class MarchingCubesDemo extends DemoApp {

  private static final int NUM_SEEDS = 16;
  
  private final Square square;
  private final DecimalFormat decimalFormat = new DecimalFormat("#.000");

  private Camera frozenCamera;
  private float gridScale = 1, gridDelta = 0;
  private int screenZoom = 6;
  private BufferedImageTexture texture;
  private long elapsed, lastTick;
  private boolean paused = false;
  private boolean shadeCells = false;
  
  private Vec3[] seedCoords = new Vec3[NUM_SEEDS];
  private FloatBuffer seeds;
  private int numSeeds = 0;
  private int draggingSeed = -1;

  private boolean record;
  private List<BufferedImage> frames;
  
  private ShaderAutoloader loader;

  protected MarchingCubesDemo() {
    super("Implicit Surface Finder - Scale = 1.000");
    this.square = new Square();
    this.square.setHasTexture(true);
    this.paused = false;
    this.record = false;
    this.elapsed = 0;
    this.lastTick = System.currentTimeMillis();

    updateSeedsBuffer();
    setCameraDistance(2.15);
    this.frozenCamera = new Camera(getCamera());
    animateCameraToPosition(new Vec3(20, 10, 20));

    String root = MarchingCubesDemo.class.getPackage().getName().replace(".", "/") + "/";
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

  // Jim Blinn's simplest blobby surface function
  float fBlinn(float r) {
    return 1.0f / (r * r);
  }
  
  // return signed implicit surface, thresholded at f=1
  float getSurface(Vec3 pt) {
    float f = 0;
    for (int i = 0; i < numSeeds; i++) {
      f += fBlinn((float) seedCoords[i].minus(pt).length());
    }
    return 1 - f;
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

  boolean isHot(Coord c) {
    return getSurface(new Vec3(gridScale * c.i, gridScale * c.j, 0)) < 0;
  }
  
  boolean isInteresting(Coord c) {
    boolean UL = isHot(c.plus(CORNERS[0]));
    boolean UR = isHot(c.plus(CORNERS[1]));
    boolean LR = isHot(c.plus(CORNERS[2]));
    boolean LL = isHot(c.plus(CORNERS[3]));
    return LL != LR || UL != UR || LL != UL || LR != UR;
  }
  
  void fillMarchingSquaresTexture() {
    if (numSeeds > 0 && getHeight() != 0) {
      int dx = (int) Math.ceil(12 * getWidth() / getHeight() / gridScale);
      int dy = (int) (12 / gridScale);
      
      // This isn't actually textured onto the quad, but it's a handy way to pass the image
      this.texture = new BufferedImageTexture(dx, dy);
      this.square.setTexture(texture);
  
      int leftmost = 0;
      for (int i = 1; i < numSeeds; i++) {
        if (seedCoords[i].getX() < seedCoords[leftmost].getX()) {
          leftmost = i;
        }
      }
      Coord start = new Coord(
          (int) (seedCoords[leftmost].getX() / gridScale), 
          (int) (seedCoords[leftmost].getY() / gridScale));
      while (!isInteresting(start) && start.i * gridScale >= -(screenZoom + 1) * getWidth() / getHeight()) {
        start = new Coord(start.i - 1, start.j);
      }
      
      List<Coord> queue = new LinkedList<>();
      Map<Coord, Integer> visited = new HashMap<>();
      int maxStep = 1;
  
      queue.add(start);
      visited.put(start, 1);
      while (!queue.isEmpty()) {
        Coord c = queue.remove(0);
        int step = visited.get(c) + 1;
        for (int n = 0; n < 4; n++) {
          if (isHot(c.plus(CORNERS[n])) != isHot(c.plus(CORNERS[(n + 1) % 4]))) {
            Coord p = c.plus(DC[n]);
            if (!visited.containsKey(p)) {
              queue.add(p);
              visited.put(p, step);
              maxStep = Math.max(maxStep, step);
            }
          }
        }
      }
      
      for (Entry<Coord, Integer> c : visited.entrySet()) {
        int x = c.getKey().i + dx / 2;
        int y = c.getKey().j + dy / 2;
        if (x >= 0 && x < dx && y >= 0 && y < dy) {
          texture.getBufferedImage().setRGB(x, y, 250 * c.getValue() / maxStep);
        }
      }
    }
  }

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
    if (gridDelta != 0) {
      if (gridDelta > 0) {
        gridDelta--;
        gridScale *= 1.023373892;
      } else if (gridDelta < 0) {
        gridDelta++;
        gridScale *= 0.97715996843;
      }
      if (gridDelta == 0) {
        double power = Math.log(gridScale) / Math.log(2);
        power = Math.round(power);
        gridScale = (float) Math.pow(2.0,  power);
      }
      setTitle("Implicit Surface Finder - Scale = " + decimalFormat.format(gridScale));
    }
    fillMarchingSquaresTexture();
    updateUniformVec2("iResolution", (float) getWidth(), (float) getHeight());
    updateUniformFloat("iGlobalTime", getIGlobalTime());
    updateUniformFloat("kScale", gridScale);
    updateUniformInt("screenZoom", screenZoom);
    updateUniformBoolean("shadeCells", shadeCells);
    updateUniformInt("numSeeds", numSeeds);
    updateUniform3fv("seeds", seeds);
    square.render(this);
  }
  
  private float getIGlobalTime() {
    return record 
        ? (frames.size() / 33.0f)
        : (elapsed / 1000.0f);
  }
  
  @Override
  public void postDraw() {
    if (record) {
      frames.add(BufferedImageRGBCanvas.copyOpenGlContextToImage(width, height, width, height));      
      if (frames.size() >= 33 * 5 /* 33 fps for five seconds */) {
        String filename = FileSystemView.getFileSystemView().getHomeDirectory() + "\\ImplicitSurfaceFinder";
        FileUtil.writeGif(frames, filename);
        record = false;
        System.out.println("Recording stopped");
      }
    }
    super.postDraw();
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
        screenZoom * ((x / (float) getWidth()) * 2 - 1) * getWidth() / getHeight(), 
        screenZoom * (((getHeight() - y) / (float) getHeight()) * 2 - 1), 
        0);
  }
  
  int findSeedOnScreen(int x, int y) {
    Vec3 pos = screenToGridSpace(x, y);
    for (int n = 0; n < numSeeds; n++) {
      if (pos.minus(seedCoords[n]).length() < 0.2) {
        return n;
      }
    }
    return -1;
  }
  
  @Override
  protected void onMouseClick(int x, int y) {
    super.onMouseClick(x, y);

    int n = findSeedOnScreen(x, y);
    if (n == -1) {
      Vec3 pos = screenToGridSpace(x, y);
      if (numSeeds == NUM_SEEDS) {
        numSeeds = 0;
      }
      seedCoords[numSeeds++] = pos;
    } else {
      seedCoords[n] = seedCoords[numSeeds - 1];
      numSeeds--;
    }
    updateSeedsBuffer();
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
    case GLFW.GLFW_KEY_R:
      if (isControlDown()) {
        record = !record;
        if (record) {
          System.out.println("Recording started");
          frames = new ArrayList<>();
        } else {
          System.out.println("Recording stopped");
        }
      }
      break;
    case GLFW.GLFW_KEY_A:
      elapsed = 0;
      break;
    case GLFW.GLFW_KEY_J:
      elapsed -= 250;
      break;
    case GLFW.GLFW_KEY_K:
      elapsed += 250;
      break;
    case GLFW.GLFW_KEY_S:
      shadeCells = !shadeCells;
      break;
    case GLFW.GLFW_KEY_LEFT_BRACKET:
      gridDelta = 30;
      break;
    case GLFW.GLFW_KEY_RIGHT_BRACKET:
      gridDelta = -30;
      break;
    case GLFW.GLFW_KEY_EQUAL:
      screenZoom = Math.max(1, screenZoom - 1);
      break;
    case GLFW.GLFW_KEY_MINUS:
      screenZoom = Math.min(10, screenZoom + 1);
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
    new MarchingCubesDemo().run();
  }
}
