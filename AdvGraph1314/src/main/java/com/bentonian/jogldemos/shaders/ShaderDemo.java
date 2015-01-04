package com.bentonian.jogldemos.shaders;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.media.opengl.GLAutoDrawable;

import com.bentonian.framework.math.M3d;
import com.bentonian.jogldemos.internals.JoglDemo;
import com.bentonian.jogldemos.internals.JoglDemoContainer;

public class ShaderDemo extends JoglDemo {

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
  }

  @Override
  public String getTitle() {
    return "Shader Demo";
  }

  @Override
  public void init(GLAutoDrawable glDrawable) {
    super.init(glDrawable);
    setCameraDistance(CAMERA_DISTANCE);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case KeyEvent.VK_1:
    case KeyEvent.VK_2:
      super.keyPressed(e);
      mandelbrotZoom = 1;
      mandelbrotCenterX = 0;
      mandelbrotCenterY = 0;
      break;
    case KeyEvent.VK_MINUS:
    case KeyEvent.VK_UNDERSCORE:
      nextShader = (currentShader + shaders.length - 1) % shaders.length;
      break;
    case KeyEvent.VK_PLUS:
    case KeyEvent.VK_EQUALS:
      nextShader = (currentShader + shaders.length + 1) % shaders.length;
      break;
    case KeyEvent.VK_OPEN_BRACKET:
      model = model.prev();
      model.dispose();
      break;
    case KeyEvent.VK_CLOSE_BRACKET:
      model = model.next();
      model.dispose();
      break;
    default: super.keyPressed(e);
      break;
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
    if ((currentShader != -1) && shaders[currentShader].getClass().getName().contains("Ping")) {
      pingPoint = pick(model.getGeometry(), e.getX(), e.getY());
    }
  }

  @Override
  protected void draw() {
    if (nextShader != -1) {
      if (currentShader != -1) {
        shaders[currentShader].disable(this);
      }
      currentShader = nextShader;
      model.dispose();
      shaders[currentShader].init(this);
      nextShader = -1;
    }

    shaders[currentShader].render(this, model);
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    if (e.isControlDown()) {
      int dx = e.getLocationOnScreen().x - lastCapturedMousePosition.x;
      int dy = e.getLocationOnScreen().y - lastCapturedMousePosition.y;
      lastCapturedMousePosition = e.getLocationOnScreen();
      mandelbrotCenterX -= dx / (10 * mandelbrotZoom);
      mandelbrotCenterY += dy / (10 * mandelbrotZoom);
    } else {
      super.mouseDragged(e);
    }
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    if (e.isControlDown()) {
      double notches = -e.getWheelRotation();
      mandelbrotZoom *= Math.pow(1.1, notches);
    } else {
      super.mouseWheelMoved(e);
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
    JoglDemoContainer.dx = 413 * 3 / 2;
    JoglDemoContainer.dy = 442 * 3 / 2;
    JoglDemoContainer.go(new ShaderDemo());
  }
}
