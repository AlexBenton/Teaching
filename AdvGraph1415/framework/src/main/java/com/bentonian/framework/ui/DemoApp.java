package com.bentonian.framework.ui;

import java.awt.Point;
import java.io.File;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.scene.CameraAnimator;
import com.google.common.collect.Sets;

public class DemoApp extends GLWindowedApp {

  protected Point lastCapturedMousePosition;
  protected Set<Integer> keysHeldDown;
  protected long lastFrameStartMillis;
  protected CameraAnimator cameraAnimator;

  private String title;

  protected DemoApp(String title) {
    this.title = title;
    this.keysHeldDown = Sets.newHashSet();
    setCameraDistance(10);
  }

  @Override
  protected void initGl() {
    super.initGl();
    Display.setTitle(title);
    Display.setResizable(true);
  }

  public void setTitle(String title) {
    this.title = title;
    Display.setTitle(title);
  }

  public String getTitle() {
    return title;
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

  @Override
  public void onKeyDown(int key) {
    double d = getCameraDistance();
    int sign = isControlDown() ? -1 : 1;

    keysHeldDown.add(key);
    switch (key) {
    default:
      super.onKeyDown(key);
      break;
    case Keyboard.KEY_1:
      cameraAnimator = new CameraAnimator(getCamera(), new M3d(0, 0, sign * d), new M3d(0, 0, -sign), new M3d(0, 1, 0), 1000);
      break;
    case Keyboard.KEY_2:
      cameraAnimator = new CameraAnimator(getCamera(), new M3d(0, sign * d, 0), new M3d(0, -sign, 0), new M3d(0, 0, -sign), 1000);
      break;
    case Keyboard.KEY_3:
      cameraAnimator = new CameraAnimator(getCamera(), new M3d(sign * d, 0, 0), new M3d(-sign, 0, 0), new M3d(0, 1, 0), 1000);
      break;
    case Keyboard.KEY_P:
      if (isControlDown()) {
        captureScreenshot(getScreenshotName());
      }
      break;
    }
  }

  @Override
  public void onKeyUp(int key) {
    keysHeldDown.remove(key);
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
        case Keyboard.KEY_NEXT:
          setCameraDistance(getCameraDistance() + step);
          break;
        case Keyboard.KEY_PRIOR:
          setCameraDistance(getCameraDistance() - step);
          break;
        case Keyboard.KEY_LEFT:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(new M3d(0, 1, 0)), -step);
          break;
        case Keyboard.KEY_RIGHT:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(new M3d(0, 1, 0)), step);
          break;
        case Keyboard.KEY_UP:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(new M3d(1, 0, 0)), -step);
          break;
        case Keyboard.KEY_DOWN:
          getCamera().rotate(getCamera().getLocalToParent().extract3x3().times(new M3d(1, 0, 0)), step);
          break;
        }
      }
    }

    lastFrameStartMillis = now;

    super.preDraw();
  }

  @Override
  protected void onMouseDown(int x, int y, int mouseButton) {
    lastCapturedMousePosition = new Point(x, y);
  }

  @Override
  protected void onMouseUp(int x, int y, int mouseButton) {
    super.onMouseUp(x, y, mouseButton);
  }

  @Override
  protected void onMouseDrag(int x, int y) {
    if (lastCapturedMousePosition != null) {
      double distanceFromOrigin = getCameraDistance();
      int dx = x - lastCapturedMousePosition.x;
      int dy = y - lastCapturedMousePosition.y;
      double len = Math.sqrt(dx*dx + dy*dy) / (12.0 * distanceFromOrigin);
      M3d axis = getCamera().getUp().times(dx).plus(getCamera().getRight().times(dy)).normalized();
      getCamera().translate(getCamera().getDirection().times(distanceFromOrigin));
      getCamera().rotate(axis, len);
      getCamera().translate(getCamera().getDirection().times(-distanceFromOrigin));
    }

    lastCapturedMousePosition = new Point(x, y);
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
    String screenshot = FileSystemView.getFileSystemView().getHomeDirectory() + "\\" + title;
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
