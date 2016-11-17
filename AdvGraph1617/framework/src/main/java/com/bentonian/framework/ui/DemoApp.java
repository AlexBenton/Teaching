package com.bentonian.framework.ui;

import static com.bentonian.framework.math.MathConstants.X_AXIS;
import static com.bentonian.framework.math.MathConstants.Y_AXIS;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.glfw.GLFW;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.PinnedText;
import com.bentonian.framework.scene.CameraAnimator;

public class DemoApp extends GLFWCanvas {

  protected long lastFrameStartMillis;
  protected CameraAnimator cameraAnimator;
  protected List<Long> frameTimestamps = new LinkedList<>();
  
  private boolean showFPS = false;
  private PinnedText FPS = new PinnedText();

  protected DemoApp(String title) {
    super(title);
    setCameraDistance(10);
  }

  public double getCameraDistance() {
    return getCamera().getPosition().length();
  }

  public void setCameraDistance(double d) {
    d = Math.max(0.001, d);
    M3d viewpoint = getCamera().getPosition();
    M3d delta = getCamera().getPosition().normalized().times(d).minus(viewpoint);
    getCamera().translate(delta);
  }
  
  public void animateCameraToPosition(M3d dest) {
    animateCameraToPosition(dest, Y_AXIS);
  }

  public void animateCameraToPosition(M3d dest, M3d up) {
    cameraAnimator = new CameraAnimator(getCamera(), dest, dest.normalized().times(-1), up, 1000);
  }

  @Override
  public void preDraw() {
    long now = System.currentTimeMillis();
    double step = (now - lastFrameStartMillis) / 500.0;
    
    if (cameraAnimator != null) {
      cameraAnimator.apply();
      if (cameraAnimator.isDone()) {
        cameraAnimator = null;
      }
    }

    if (lastFrameStartMillis != 0) {
      for (int key : keysHeldDown) {
        switch (key) {
        default:
          break;
        case GLFW.GLFW_KEY_PAGE_UP:
          setCameraDistance(getCameraDistance() + 8 * step);
          break;
        case GLFW.GLFW_KEY_PAGE_DOWN:
          setCameraDistance(getCameraDistance() - 8 * step);
          break;
        case GLFW.GLFW_KEY_LEFT:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(Y_AXIS), -step);
          break;
        case GLFW.GLFW_KEY_RIGHT:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(Y_AXIS), step);
          break;
        case GLFW.GLFW_KEY_UP:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(X_AXIS), -step);
          break;
        case GLFW.GLFW_KEY_DOWN:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(X_AXIS), step);
          break;
        }
      }
    }

    lastFrameStartMillis = now;
    frameTimestamps.add(now);
    while (frameTimestamps.get(0) < now - 5000) {
      frameTimestamps.remove(0);
    }

    super.preDraw();
  }

  @Override
  protected void postDraw() {
    if (showFPS && frameTimestamps.size() > 1) {
      float seconds = 
          (float) (frameTimestamps.get(frameTimestamps.size() - 1) - frameTimestamps.get(0)) / 1000;
      float fps = ((float) frameTimestamps.size()) / seconds;
      FPS.setText("FPS: " + String.format("%.2f", fps));
      FPS.render(this);
    }

    super.postDraw();
  }

  @Override
  public void onKeyDown(int key) {
    double d = getCameraDistance();
    int sign = isControlDown() ? -1 : 1;

    switch (key) {
    default:
      super.onKeyDown(key);
      break;
    case GLFW.GLFW_KEY_1:
      animateCameraToPosition(new M3d(0, 0, sign * d));
      break;
    case GLFW.GLFW_KEY_2:
      animateCameraToPosition(new M3d(0, sign * d, 0), new M3d(0, 0, -sign));
      break;
    case GLFW.GLFW_KEY_3:
      animateCameraToPosition(new M3d(sign * d, 0, 0));
      break;
    case GLFW.GLFW_KEY_P:
      if (isControlDown()) {
        captureScreenshot(getScreenshotName());
      }
      break;
    case GLFW.GLFW_KEY_F:
      showFPS = !showFPS;
      break;
    }
  }

  @Override
  protected void onMouseDrag(int x, int y) {
    if (mouseDownCaptured) {
      double distanceFromOrigin = getCameraDistance();
      int dx = x - lastCapturedMousePosition.x;
      int dy = y - lastCapturedMousePosition.y;
      double len = Math.sqrt(dx*dx + dy*dy) / (12.0 * distanceFromOrigin);
      M3d axis = getCamera().getUp().times(dx).plus(getCamera().getRight().times(dy)).normalized();
      getCamera().translate(getCamera().getDirection().times(distanceFromOrigin));
      getCamera().rotate(axis, len);
      getCamera().translate(getCamera().getDirection().times(-distanceFromOrigin));
    }

    super.onMouseDrag(x, y);
  }

  @Override
  public void onMouseWheel(int delta) {
    double notches = (delta < 0) ? 0.4 : (delta > 0) ? -0.4 : 0;

    if (isControlDown()) {
      notches /= 10;
    }
    setCameraDistance(getCameraDistance() + notches);
  }

  private String getScreenshotName() {
    int i = 0;
    String screenshot = FileSystemView.getFileSystemView().getHomeDirectory() + "\\" + getTitle();
    while (new File(screenshot + "(" + i + ").png").exists()) {
      i++;
    }
    return screenshot + "(" + i + ")" + ".png";
  }

  private void captureScreenshot(String file) {
    GLFrameBuffer frameBuffer = new GLFrameBuffer(getWidth(), getHeight());
    pushFrameBuffer(frameBuffer);
    mainLoop();
    popFrameBuffer(frameBuffer);
    BufferedImageRGBCanvas.copyFrameBufferToImage(frameBuffer).write(file);
  }
}
