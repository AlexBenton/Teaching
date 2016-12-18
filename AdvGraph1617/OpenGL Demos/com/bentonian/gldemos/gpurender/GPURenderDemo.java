package com.bentonian.gldemos.gpurender;

import static com.bentonian.framework.ui.ShaderUtil.compileProgram;
import static com.bentonian.framework.ui.ShaderUtil.loadShader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.io.FileUtil;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.ui.BufferedImageRGBCanvas;
import com.bentonian.framework.ui.DemoApp;

public class GPURenderDemo extends DemoApp {

  private static final String[] SHADERS = { 
    "bending.fsh",
    "blending.fsh",
    "repetition.fsh",
    "repetition2.fsh",
    "ambient occlusion.fsh",
    "dancing cubes.fsh", 
    "voronoi cells.fsh",
    "refractive blobbies.fsh", 
    "reflection and refraction.fsh", 
    "primitives.fsh", 
    "noise.fsh",
    "raytracing.fsh", 
  };

  private final Square square;

  private Camera frozenCamera;
  private boolean showRenderDepth;
  private int currentShader;
  private long elapsed, lastTick;
  private boolean paused;
  
  private boolean record;
  private long recordingStartedAt;
  private List<BufferedImage> frames;

  private volatile long lastFailedTimestamp = 0;
  private volatile long lastLoadedTimestamp = 0;

  protected GPURenderDemo() {
    super("GPU Render");
    this.square = new Square();
    this.square.setHasTexture(true);
    this.currentShader = 0;
    this.paused = false;
    this.record = false;
    this.elapsed = 0;
    this.lastTick = System.currentTimeMillis();

    setCameraDistance(2.15);
    this.frozenCamera = new Camera(getCamera());
    animateCameraToPosition(new M3d(20, 10, 20));

    // This isn't actually textured onto the quad, but it's a handy way to pass the image
    this.square.setTexture(new BufferedImageTexture(GPURenderDemo.class, "background.jpg"));
    
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
  public void postDraw() {
    if (record) {
      frames.add(BufferedImageRGBCanvas.copyOpenGlContextToImage(width, height, width, height));      
      if (System.currentTimeMillis() - recordingStartedAt > 10000) {
        String filename = FileSystemView.getFileSystemView().getHomeDirectory() + "\\" + SHADERS[currentShader].replace(".fsh",  "");
        FileUtil.writeGif(frames, filename);
        record = false;
        System.out.println("Recording stopped");
      }
      
      // Clamp to 33FPS
      while (System.currentTimeMillis() - lastTick > 33) {
        try {
          Thread.sleep(1);
        } catch (Exception e) { }
      }
    }
    super.postDraw();
  }

  @Override
  protected void onResized(int width, int height) {
    super.onResized(width, height);
    square.setIdentity();
    square.scale(new M3d(width / (float) height, 1, 1));
    if (getProgram() != -1) {
      updateUniformVec2("iResolution", (float) width, (float) height);
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
          recordingStartedAt = System.currentTimeMillis();
          frames = new ArrayList<>();
        } else {
          System.out.println("Recording stopped");
        }
      } else {
        showRenderDepth = !showRenderDepth;
        updateUniformBoolean("iShowRenderDepth", showRenderDepth);
      }
      break;
    case GLFW.GLFW_KEY_4:
      animateCameraToPosition(new M3d(2, 1, 2).normalized().times(8));
      break;
    case GLFW.GLFW_KEY_LEFT_BRACKET:
      currentShader = (currentShader + SHADERS.length - 1) % SHADERS.length;
      loadShaderProgram(SHADERS[currentShader]);
      break;
    case GLFW.GLFW_KEY_RIGHT_BRACKET:
      currentShader = (currentShader + 1) % SHADERS.length;
      loadShaderProgram(SHADERS[currentShader]);
      break;
    case GLFW.GLFW_KEY_SPACE:
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
      int program = compileProgram(vsName, fsName);

      useProgram(program);
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
    return FileUtil.getFileTimestampHash(getRoot() + SHADERS[currentShader])
        ^ FileUtil.getDirectoryTimestampHash(getRoot() + "include/");
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
