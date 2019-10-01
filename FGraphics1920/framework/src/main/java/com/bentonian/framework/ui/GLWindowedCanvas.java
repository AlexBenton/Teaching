package com.bentonian.framework.ui;

import java.awt.Point;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersectionList;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.scene.PrimitiveCollection;
import com.google.common.base.Preconditions;

/**
 * All things "UI"--mouse, keyboard, drags, app loop, app closure, window sizes,
 * etc.
 */
public class GLWindowedCanvas extends GLCanvas implements Runnable {

  protected String title;

  protected int left = 200;
  protected int top = 200;
  protected int width = 800;
  protected int height = 600;

  protected boolean exitRequested = false;
  protected boolean isControlDown = false;
  protected boolean isLeftMouseDown = false;
  protected Set<Integer> keysHeldDown = new HashSet<>();
  protected Point lastCapturedMousePosition;
  protected Point lastMouseDownPosition;
  protected boolean mouseDownCaptured;

  private PrimitiveCollection mouseHandlers = new PrimitiveCollection();
  private MouseEventHandler currentMouseCaptureHandler;
  private MouseEventHandler currentMouseOverHandler;
  
  private List<GLWindowedAppSecondaryFrame> secondaryFrames = new LinkedList<>();

  public GLWindowedCanvas(String title) {
    this.title = title;
  }

  @Override
  public void run() {
    initGl();
    while (!isExitRequested()) {
      mainLoop();
    }
    shutdownGl();
  }

  @Override
  protected void initGl() {
    super.initGl();
    onResized(width, height);
  }

  public void mainLoop() {
    preDraw();
    draw();
    postDraw();
  }

  protected void preDraw() {
    updateUniforms();
  }

  protected void draw() {
  }

  protected void postDraw() {
  }

  @Override
  public void shutdownGl() {
    requestExit();
    super.shutdownGl();
  }

  /**
   * Note that we require that any handler must extend both Primitive (for hit
   * testing) and MouseEventHandler (for event handling).
   */
  public <T extends Primitive & MouseEventHandler> void registerMouseHandler(T handler) {
    mouseHandlers.add(handler);
  }

  public <T extends Primitive & MouseEventHandler> void removeMouseHandler(T handler) {
    mouseHandlers.remove(handler);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getLeft() {
    return left;
  }

  public int getTop() {
    return top;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  protected void deliverOnMouseDown(int x, int y) {
    Ray ray = getCameraRay(x, y);
    RayIntersectionList hits = RayTracerEngine.traceScene(mouseHandlers, ray).sorted();
    if (!hits.isEmpty()) {
      currentMouseCaptureHandler = (MouseEventHandler) hits.getHead().primitive;
      currentMouseCaptureHandler.onMouseDown(getCamera(), ray);
    } else {
      onMouseDown(x, y);
    }
  }

  protected void deliverOnMouseUp(int x, int y) {
    if (currentMouseCaptureHandler != null) {
      Ray ray = getCameraRay(x, y);
      currentMouseCaptureHandler.onMouseUp(getCamera(), ray);
      currentMouseCaptureHandler = null;
    } else {
      onMouseUp(x, y);
    }
  }

  protected void deliverOnMouseMove(int x, int y) {
    Ray ray = getCameraRay(x, y);
    MouseEventHandler hit = null;
    RayIntersectionList hits = RayTracerEngine.traceScene(mouseHandlers, ray).sorted();
    if (!hits.isEmpty()) {
      hit = (MouseEventHandler) hits.getHead().primitive;
    }
    if (currentMouseOverHandler != hit) {
      if (currentMouseOverHandler != null) {
        currentMouseOverHandler.onMouseOut(camera, ray);
      }
      currentMouseOverHandler = hit;
    }
    if (currentMouseOverHandler != null) {
      currentMouseOverHandler.onMouseOver(camera, ray);
    }
    onMouseMove(x, y);
  }

  protected void deliverOnMouseDrag(int x, int y) {
    if (currentMouseCaptureHandler != null) {
      Ray ray = getCameraRay(x, y);
      currentMouseCaptureHandler.onMouseDrag(getCamera(), ray);
    } else {
      onMouseDrag(x, y);
    }
  }

  public void addSecondaryFrame(GLWindowedAppSecondaryFrame frame) {
    secondaryFrames.add(frame);
  }

  public void requestExit() {
    if (!exitRequested) {
      exitRequested = true;
      for (GLWindowedAppSecondaryFrame frame : secondaryFrames) {
        frame.dispose();
      }
    }
  }

  public boolean isExitRequested() {
    return exitRequested;
  }

  protected void onKeyDown(int key) {
    keysHeldDown.add(key);
    if (key == GLFW.GLFW_KEY_ESCAPE) {
      requestExit();
    }
  }

  protected void onKeyUp(int key) {
    keysHeldDown.remove(key);
  }

  protected void onMouseDown(int x, int y) {
    lastCapturedMousePosition = new Point(x, y);
    lastMouseDownPosition = lastCapturedMousePosition;
    mouseDownCaptured = true;
  }

  protected void onMouseUp(int x, int y) {
    mouseDownCaptured = false;
    if (lastMouseDownPosition != null 
        && x == lastMouseDownPosition.x 
        && y == lastMouseDownPosition.y) {
      onMouseClick(x, y);
    }
  }

  protected void onMouseDrag(int x, int y) {
    lastCapturedMousePosition = new Point(x, y);
  }

  protected void onMouseMove(int x, int y) {
    lastCapturedMousePosition = new Point(x, y);
  }
  
  public Point getLastCapturedMousePosition() {
    return lastCapturedMousePosition;
  }

  protected void onMouseClick(int x, int y) {
  }

  protected void onMouseWheel(int delta) {
  }

  public boolean isControlDown() {
    return isControlDown;
  }

  public Ray getCameraRay(int x, int y) {
    return RayTracerEngine.getCameraRay(getCamera(), x, ((int) height) - y, width, height);
  }

  public RayIntersections pick(Primitive scene, int x, int y) {
    return RayTracerEngine.traceScene(scene, getCameraRay(x, y));
  }

  public Primitive pickPrimitive(Primitive scene, int x, int y) {
    RayIntersections hits = pick(scene, x, y);
    return hits.isEmpty() ? null : hits.getNearest().primitive;
  }

  public Vec3 pickPoint(Primitive scene, int x, int y) {
    RayIntersections hits = pick(scene, x, y);
    return hits.isEmpty() ? null : hits.getNearest().point;
  }

  public GLWindowedCanvas setWindowPos(int left, int top, int width, int height) {
    this.left = left;
    this.top = top;
    onResized(width, height);
    return this;
  }

  protected void onResized(int width, int height) {
    this.width = width;
    this.height = height;

    Preconditions.checkState(getProjection().size() >= 1);
    getProjection().peek().setData(M4x4.perspective((float) width / (float) height));
  }
}
