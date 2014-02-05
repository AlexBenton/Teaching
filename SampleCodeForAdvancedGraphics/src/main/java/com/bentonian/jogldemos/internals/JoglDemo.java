package com.bentonian.jogldemos.internals;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.filechooser.FileSystemView;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.ui.GLBufferedImageCanvas;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.raytrace.engine.RayTracerEngine;

public class JoglDemo extends GLRenderingContext implements
    KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

  protected Point lastCapturedMousePosition;
  protected boolean spinning = true;

  private final String title;
  private int width, height;
  private boolean dragLockedToHorizontal;
  private boolean dragLockedToVertical;
  private long tick;
  private String screenshot;

  protected JoglDemo(String title) {
    this.title = title;
    this.screenshot = null;
  }

  @Override
  public void init(GLAutoDrawable glDrawable) {
    super.init(glDrawable);
    getCamera().translate(new M3d(0, 0, 10));
  }

  public String getTitle() {
    return title;
  }

  public double getCameraDistance() {
    return getCamera().getPosition().length();
  }

  public void setCameraDistance(double d) {
    M3d viewpoint = getCamera().getPosition();
    M3d delta = getCamera().getPosition().normalized().times(d).minus(viewpoint);
    getCamera().translate(delta);
  }

  @Override
  public void keyPressed(KeyEvent e) {
    double d = getCameraDistance();
    switch (e.getKeyCode()) {
    case KeyEvent.VK_ESCAPE:
      System.exit(0);
      break;
    case KeyEvent.VK_SPACE:
      spinning = !spinning;
      break;
    case KeyEvent.VK_PAGE_DOWN:
      setCameraDistance(d + 0.25);
      break;
    case KeyEvent.VK_PAGE_UP:
      setCameraDistance(d - 0.25);
      break;
    case KeyEvent.VK_1:
      getCamera().setIdentity().translate(new M3d(0, 0, 1));
      setCameraDistance(d);
      break;
    case KeyEvent.VK_2:
      getCamera().setIdentity().translate(new M3d(0, 0, 1)).rotate(new M3d(1, 0, 0), PI/2.0);
      setCameraDistance(d);
      break;
    case KeyEvent.VK_P:
      if (e.isControlDown()) {
        int i = 0;
        screenshot = FileSystemView.getFileSystemView().getHomeDirectory() + "\\" + title;
        while (new File(screenshot + "(" + i + ").png").exists()) {
          i++;
        }
        screenshot = screenshot + "(" + i + ")" + ".png";
      }
      break;
    }
  }

  @Override
  protected void postDraw() {
    if (screenshot != null) {
      GLBufferedImageCanvas imageCopy = new GLBufferedImageCanvas(width, height);
      imageCopy.copyContextToCanvas(gl);
      imageCopy.write(screenshot);
      screenshot = null;
    }
  }

  @Override
  public void keyReleased(KeyEvent e) {
  }

  @Override
  public void keyTyped(KeyEvent e) {
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    lastCapturedMousePosition = e.getLocationOnScreen();
    dragLockedToHorizontal = dragLockedToVertical = false;
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    double distanceFromOrigin = getCameraDistance();
    int dx = e.getLocationOnScreen().x - lastCapturedMousePosition.x;
    int dy = e.getLocationOnScreen().y - lastCapturedMousePosition.y;
    if (e.isShiftDown() && !dragLockedToHorizontal && !dragLockedToVertical) {
      if (abs(dx) > abs(dy)) {
        dragLockedToHorizontal = true;
      } else {
        dragLockedToVertical = true;
      }
    }
    if (dragLockedToHorizontal) {
      dy = 0;
    } else if (dragLockedToVertical) {
      dx = 0;
    }
    double len = Math.sqrt(dx*dx + dy*dy) / (12.0 * distanceFromOrigin);
    M3d axis = getCamera().getUp().times(dx).plus(getCamera().getRight().times(dy)).normalized();

    lastCapturedMousePosition = e.getLocationOnScreen();
    getCamera().translate(getCamera().getDirection().times(distanceFromOrigin));
    getCamera().rotate(axis, len);
    getCamera().translate(getCamera().getDirection().times(-distanceFromOrigin));
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    double notches = e.getWheelRotation() / 1.5;

    if (e.isControlDown()) {
      notches /= 10;
    }
    if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
      setCameraDistance(getCameraDistance() + notches);
    }
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }

  public M3d pick(Primitive scene, int x, int y) {
    Ray ray = RayTracerEngine.getCameraRay(getCamera(), x, ((int) height) - y, width, height);
    RayIntersections hits = RayTracerEngine.traceScene(scene, ray);
    return hits.isEmpty() ? null : hits.getNearest().point;
  }

  @Override
  public void display(GLAutoDrawable glDrawable) {
    if (!spinning) {
      long now = System.currentTimeMillis();

      if (tick == 0) {
        tick = now;
      }
      double distanceFromOrigin = getCameraDistance();
      getCamera().translate(getCamera().getDirection().times(distanceFromOrigin));
      getCamera().rotate(getCamera().getUp(), -((now - tick) / 1000.0) * PI / 4.0);
      getCamera().translate(getCamera().getDirection().times(-distanceFromOrigin));
      tick = now;
    }
    super.display(glDrawable);
  }

  @Override
  public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
    this.width = width;
    this.height = height;
    super.reshape(glDrawable, x, y, width, height);
  }
  
  protected int getWidth() {
    return width;
  }
  
  protected int getHeight() {
    return height;
  }
}
