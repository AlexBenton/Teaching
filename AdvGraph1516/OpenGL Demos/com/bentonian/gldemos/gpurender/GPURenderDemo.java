package com.bentonian.gldemos.gpurender;

import static com.bentonian.framework.ui.ShaderUtil.compileProgram;
import static com.bentonian.framework.ui.ShaderUtil.loadShader;
import static com.bentonian.framework.ui.ShaderUtil.testGlError;

import java.io.File;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.ui.DemoApp;

public class GPURenderDemo extends DemoApp {

  private static final String[] SHADERS = { "reflection and refraction.fsh", "primitives.fsh", "noise.fsh", "sdf.fsh", "sdf-blobbies.fsh" };

  private final Square square;

  private int activeProgram;
  private Camera frozenCamera;
  private boolean showRenderDepth;
  private boolean paused;
  private int currentShader;
  private long elapsed, lastTick;

  private volatile long lastFailedTimestamp = 0;
  private volatile long lastLoadedTimestamp = 0;

  protected GPURenderDemo() {
    super("GPU Render");
    this.activeProgram = -1;
    this.square = new Square();
    this.square.setHasTexture(true);
    this.currentShader = 0;
    this.paused = false;
    this.elapsed = 0;
    this.lastTick = System.currentTimeMillis();

    setCameraDistance(2.15);
    this.frozenCamera = new Camera(getCamera());
    animateCameraToPosition(new M3d(20, 10, 20));

    new Reloader().start();
  }

  @Override
  protected Camera getCameraForModelview() {
    return frozenCamera;
  }

  @Override
  public void preDraw() {
    if (lastLoadedTimestamp == 0) {
      loadShaderProgram(SHADERS[currentShader]);
    }
    super.preDraw();
  }

  @Override
  public void draw() {
    M3d camPos = getCamera().getPosition();
    M3d camDir = getCamera().getDirection();
    M3d camUp = getCamera().getUp();
    long now = System.currentTimeMillis();

    if (!paused) {
      elapsed += now - lastTick;
    }
    lastTick = now;
    updateUniformFloat("iGlobalTime", ((float) elapsed) / 1000.0f);
    updateUniformVec3("iRayOrigin", camPos);
    updateUniformVec3("iRayDir", camDir);
    updateUniformVec3("iRayUp", camUp);
    square.render(this);
  }

  @Override
  protected void onResized(int width, int height) {
    super.onResized(width, height);
    square.setIdentity();
    square.scale(new M3d(width / (float) height, 1, 1));
    if (activeProgram != -1) {
      updateUniformVec2("iResolution", (float) width, (float) height);
    }
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case Keyboard.KEY_R:
      showRenderDepth = !showRenderDepth;
      updateUniformBoolean("iShowRenderDepth", showRenderDepth);
      break;
    case Keyboard.KEY_4:
      animateCameraToPosition(new M3d(2, 1, 2).normalized().times(8));
      break;
    case Keyboard.KEY_LBRACKET:
      currentShader = (currentShader + SHADERS.length - 1) % SHADERS.length;
      loadShaderProgram(SHADERS[currentShader]);
      break;
    case Keyboard.KEY_RBRACKET:
      currentShader = (currentShader + 1) % SHADERS.length;
      loadShaderProgram(SHADERS[currentShader]);
      break;
    case Keyboard.KEY_SPACE:
      paused = !paused;
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  private void loadShaderProgram(String fragmentShaderFilename) {
    try {
      String root = getRoot();
      int vsName = loadShader(GL20.GL_VERTEX_SHADER, root + "include/basic.vsh");
      int fsName = loadShader(GL20.GL_FRAGMENT_SHADER, root + fragmentShaderFilename);

      int prog = compileProgram(vsName, fsName);
      testGlError();
      useProgram(prog);
      activeProgram = prog;
      updateUniformVec2("iResolution", (float) getWidth(), (float) getHeight());
      System.out.println("Succesfully loaded '" + fragmentShaderFilename + "'");
      lastLoadedTimestamp = getCurrentShaderTimestamp();
      lastFailedTimestamp = 0;
    } catch (RuntimeException e) {
      if (e.getMessage() != null) {
        System.err.println(e.getMessage());
      } else if (e instanceof NullPointerException) {
        System.out.println("File not found: '" + fragmentShaderFilename + "'");
      } else {
        System.out.println("Unspecified error while loading '" + fragmentShaderFilename + "'");
        e.printStackTrace();
      }
      lastLoadedTimestamp = -1;
      lastFailedTimestamp = getCurrentShaderTimestamp();
    }
  }

  private String getRoot() {
    return GPURenderDemo.class.getPackage().getName().replace(".", "/") + "/";
  }

  private synchronized long getCurrentShaderTimestamp() {
    try {
      return new File(getRoot() + SHADERS[currentShader]).lastModified();
    } catch (Exception e) {
      return -1;
    }
  }
  
  private class Reloader extends Thread {
    @Override
    public void run() {
      while (!exitRequested) {
        if (lastLoadedTimestamp != 0 || lastFailedTimestamp != 0) {
          long timestamp = getCurrentShaderTimestamp();
          if (timestamp != -1) {
            if ((lastFailedTimestamp != 0 && lastFailedTimestamp != timestamp)
                || (lastFailedTimestamp == 0 && lastLoadedTimestamp != timestamp)) {
              lastLoadedTimestamp = 0;
              lastFailedTimestamp = 0;
            }
          }
        }
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) { }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new GPURenderDemo().run();
  }
}
