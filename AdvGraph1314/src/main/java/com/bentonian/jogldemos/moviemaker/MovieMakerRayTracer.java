package com.bentonian.jogldemos.moviemaker;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.ui.GLImageBufferCanvas;
import com.bentonian.raytrace.engine.RayTracerEngine;
import com.bentonian.raytrace.engine.Scene;

public class MovieMakerRayTracer extends Frame {

  private static final String TITLE = "Ray Tracer";

  private final RayTracerEngine engine;

  private GLImageBufferCanvas canvas;
  private boolean deferredRenderRequested = false;

  public MovieMakerRayTracer(final MovieMakerBackbone backbone) {
    super(TITLE);

    this.canvas = new GLImageBufferCanvas(32, 32);

    addComponentListener(new ComponentListener() {
      @Override public void componentResized(ComponentEvent e) {
        canvas = new GLImageBufferCanvas(getWidth() - 16, getHeight() - 38);
        engine.setCanvas(canvas);
      }
      @Override public void componentHidden(ComponentEvent arg0) { }
      @Override public void componentMoved(ComponentEvent arg0) { }
      @Override public void componentShown(ComponentEvent arg0) { }
    });

    this.engine = new RayTracerEngine(
        new Scene() {
          @Override
          public List<Primitive> getPrimitives() {
            return backbone.getScene().getPrimitives();
          }
          @Override
          public List<M3d> getLights() {
            return backbone.getScene().getLights();
          }
        },
        canvas, backbone.getEditor().getCamera());
    
    forkProgressMonitorThread();
  }

  public void setSupersample(int multiple) {
    engine.setSupersamplingMultiple(multiple);
    render();
  }

  public void setImageSize(int width, int height) {
    super.setSize(width + 16, height + 38);
  }

  @Override
  public void paint(Graphics g) {
    if (deferredRenderRequested) {
      g.clearRect(0, 0, getWidth(), getHeight());
    } else {
      g.drawImage(canvas, 8, 30, this);
    }
  }

  public RayTracerEngine getRayTracer() {
    return engine;
  }

  public GLImageBufferCanvas getCanvas() {
    return canvas;
  }

  public void render() {
    deferredRenderRequested = true;
  }

  public void update() {
    if (deferredRenderRequested) {
      engine.renderToCanvas();
      deferredRenderRequested = false;
      repaint();
    }
  }

  private void forkProgressMonitorThread() {
    new Thread() {
      @Override
      public void run() {
        while (true) {
          if (engine.getRenderProgress() != 100) {
            setTitle(TITLE + " (Rendering: " + engine.getRenderProgress() + "%)");
          } else {
            setTitle(TITLE);
          }
          try {
            Thread.sleep(1000);
          } catch (Exception e) {
          }
        }
      }
    }.start();
  }
}
