package com.bentonian.gldemos.shaders;

import static com.bentonian.framework.ui.ShaderUtil.testGlError;

import java.awt.Point;

import org.lwjgl.input.Keyboard;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.DemoApp;

public class ShaderDemo extends DemoApp {

  static private final double CAMERA_DISTANCE = 5.0;

  private static final ShaderRenderer[] shaders = {
    new ShaderRenderer("basic.vsh", "basic.fsh"),
    new ShaderRenderer("phong.vsh", "phong.fsh"),
    new GoochRenderer(),
    new ShaderRenderer("lattice.vsh", "lattice.fsh"),
    new PingRenderer(),
    new MandelbrotRenderer()
  };

  private ShaderModel model;
  private int currentShader = -1;
  private int nextShader = -1;
  private float mandelbrotZoom = 1;
  private float mandelbrotCenterX = 0;
  private float mandelbrotCenterY = 0;
  private M3d pingPoint;

  public ShaderDemo() {
    super("Shader demo");
    this.nextShader = 0;
    this.model = ShaderModel.SPHERE;
    setCameraDistance(CAMERA_DISTANCE);
  }

  @Override
  public String getTitle() {
    return "Shader Demo";
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case Keyboard.KEY_1:
    case Keyboard.KEY_2:
      super.onKeyDown(key);
      mandelbrotZoom = 1;
      mandelbrotCenterX = 0;
      mandelbrotCenterY = 0;
      break;
    case Keyboard.KEY_MINUS:
      nextShader = (currentShader + shaders.length - 1) % shaders.length;
      break;
    case Keyboard.KEY_EQUALS:
      nextShader = (currentShader + shaders.length + 1) % shaders.length;
      break;
    case Keyboard.KEY_LBRACKET:
      model = model.prev();
      model.dispose();
      break;
    case Keyboard.KEY_RBRACKET:
      model = model.next();
      model.dispose();
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  @Override
  public void onMouseMove(int x, int y) {
    super.onMouseMove(x, y);
    if ((currentShader != -1) && shaders[currentShader].getClass().getName().contains("Ping")) {
      pingPoint = pickPoint(model.getGeometry(), x, y);
    }
  }

  @Override
  protected void draw() {
    if (nextShader != -1) {
      if (currentShader != -1) {
        shaders[currentShader].disable(this);
        testGlError();
      }
      currentShader = nextShader;
      model.dispose();
      testGlError();
      shaders[currentShader].init(this);
      testGlError();
      nextShader = -1;
    }

    shaders[currentShader].render(this, model);
  }

  @Override
  public void onMouseDrag(int x, int y) {
    if (isControlDown()) {
      int dx = x - lastCapturedMousePosition.x;
      int dy = y - lastCapturedMousePosition.y;
      lastCapturedMousePosition = new Point(x, y);
      mandelbrotCenterX -= dx / (10 * mandelbrotZoom);
      mandelbrotCenterY += dy / (10 * mandelbrotZoom);
    } else {
      super.onMouseDrag(x, y);
    }
  }

  @Override
  public void onMouseWheel(int delta) {
    if (isControlDown()) {
      double notches = -delta;
      mandelbrotZoom *= Math.pow(1.1, notches);
    } else {
      super.onMouseWheel(delta);
    }
  }

  float getMandelbrotZoom() {
    return mandelbrotZoom;
  }

  float getMandelbrotCenterX() {
    return mandelbrotCenterX;
  }

  float getMandelbrotCenterY() {
    return mandelbrotCenterY;
  }

  M3d getPingPoint() {
    return pingPoint;
  }

  public static void main(String[] args) {
    new ShaderDemo().run();
  }
}
