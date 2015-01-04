package com.bentonian.framework.ui;

import static com.bentonian.framework.ui.ShaderUtil.testGlError;

import java.util.List;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.PixelFormat;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.scene.Primitive;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class GLWindowedCanvas extends GLCanvas {

  protected int left = 200;
  protected int top = 200;
  protected int width = 800;
  protected int height = 600;
  protected boolean isRunning = false;

  private GLContext context = new GLContext();
  private List<MouseEventHandler> mouseHandlers = Lists.newArrayList();

  @Override
  protected void initGl() {
    try {
      Display.setDisplayMode(new DisplayMode(width, height));
      Display.setLocation(left, top);
      Display.create(new PixelFormat(), getContextAttribs());
      GLContext.useContext(context, false);
      testGlError();
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }

    super.initGl();
    testGlError();

    isRunning = true;
    onResized(width, height);
  }

  public void mainLoop() {
    pollInput();
    preDraw();
    draw();
    postDraw();
  }

  protected void preDraw() {
    testGlError();

    try {
      Display.makeCurrent();
    } catch (LWJGLException e) {
      throw new RuntimeException(e);
    }

    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    updateUniforms();
  }

  protected void draw() {
  }

  protected void postDraw() {
    testGlError();
    Display.sync(60);
    Display.update();
  }
  
  public void registerMouseHandler(MouseEventHandler handler) {
    mouseHandlers.add(handler);
  }

  public void removeMouseHandler(MouseEventHandler handler) {
    mouseHandlers.remove(handler);
  }

  protected int getWidth() {
    return width;
  }

  protected int getHeight() {
    return height;
  }

  protected void pollInput() {
    if (Display.wasResized() && (Display.getWidth() != width || Display.getHeight() != height)) {
      onResized(Display.getWidth(), Display.getHeight());
    }

    while (Keyboard.next()) {
      if (Keyboard.getEventKeyState()) {
        onKeyDown(Keyboard.getEventKey());
      } else {
        onKeyUp(Keyboard.getEventKey());
      }
    }

    while (Mouse.next()) {
      int x = Mouse.getEventX();
      int y = height - Mouse.getEventY();
      if (Mouse.getEventButton() != -1) {
        if (Mouse.getEventButtonState()) {
          deliverOnMouseDown(x, y, Mouse.getEventButton());
        } else {
          deliverOnMouseUp(x, y, Mouse.getEventButton());
        }
      } else {
        if (Mouse.isButtonDown(0)) {
          deliverOnMouseDrag(x, y);
        } else {
          deliverOnMouseMove(x, y);
        }
      }
      int dWheel = Mouse.getDWheel();
      if (dWheel != 0 ) {
        onMouseWheel(dWheel);
      }
    }
  }

  private void deliverOnMouseDown(int x, int y, int mouseButton) {
    if (mouseButton == 0 && !mouseHandlers.isEmpty()) {
      Ray ray = getCameraRay(x, y);
      for (MouseEventHandler handler : mouseHandlers) {
        if (handler.onMouseDown(getCamera(), ray)) {
          return;
        }
      }
    }
    onMouseDown(x, y, mouseButton);
  }

  private void deliverOnMouseUp(int x, int y, int mouseButton) {
    if (mouseButton == 0 && !mouseHandlers.isEmpty()) {
      Ray ray = getCameraRay(x, y);
      for (MouseEventHandler handler : mouseHandlers) {
        if (handler.onMouseUp(getCamera(), ray)) {
          return;
        }
      }
    }
    onMouseUp(x, y, mouseButton);
  }

  private void deliverOnMouseMove(int x, int y) {
    if (!mouseHandlers.isEmpty()) {
      Ray ray = getCameraRay(x, y);
      for (MouseEventHandler handler : mouseHandlers) {
        if (handler.onMouseMove(getCamera(), ray)) {
          return;
        }
      }
    }
    onMouseMove(x, y);
  }

  private void deliverOnMouseDrag(int x, int y) {
    if (!mouseHandlers.isEmpty()) {
      Ray ray = getCameraRay(x, y);
      for (MouseEventHandler handler : mouseHandlers) {
        if (handler.onMouseDrag(getCamera(), ray)) {
          return;
        }
      }
    }
    onMouseDrag(x, y);
  }
  
  protected void onKeyDown(int key) {
  }

  protected void onKeyUp(int key) {
  }

  protected void onMouseDown(int x, int y, int mouseButton) {
  }

  protected void onMouseUp(int x, int y, int mouseButton) {
  }

  protected void onMouseMove(int x, int y) {
  }

  protected void onMouseDrag(int x, int y) {
  }

  protected void onMouseWheel(int delta) {
  }

  public static boolean isControlDown() {
    return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
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

  public M3d pickPoint(Primitive scene, int x, int y) {
    RayIntersections hits = pick(scene, x, y);
    return hits.isEmpty() ? null : hits.getNearest().point;
  }

  public void shutdownGl() {
    Display.destroy();
    isRunning = false;
  }

  public GLWindowedCanvas setWindowPos(int left, int top, int width, int height) {
    if (isRunning) {
      try {
        Display.setDisplayMode(new DisplayMode(width, height));
        Display.setLocation(left, top);
      } catch (LWJGLException e) {
        throw new RuntimeException(e);
      }
    }
    this.left = left;
    this.top = top;
    onResized(width, height);
    return this;
  }

  private void onResized(int width, int height) {
    this.width = width;
    this.height = height;

    Preconditions.checkState(getProjection().size() == 1);
    getProjection().peek().setData(M4x4.perspective((float) width / (float) height));

    if (isRunning) {
      GL11.glViewport(0, 0, width, height);
      updateProjectionMatrix();
    }
  }

  private ContextAttribs getContextAttribs() {
    // Bug in LWJGL on OSX returns a 2.1 context if you ask for 3.3, but returns 4.1 if you ask for 3.2
    String osName = System.getProperty("os.name");
    boolean macHack = osName.startsWith("Mac") || osName.startsWith("Darwin");
    return new ContextAttribs(3, macHack ? 2 : 3)
        .withProfileCore(false)
        .withDebug(true);
  }
}
