package com.bentonian.jogldemos.internals;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.awt.GLCanvas;


public class JoglDemoContainer {
  public static int dx = 640, dy = 480;

  public static Frame go(JoglDemo toRender) {
    return go(toRender, dx, dy);
  }

  public static Frame go(JoglDemo toRender, int dx, int dy) {

    final GLCanvas canvas = new GLCanvas();
    canvas.addGLEventListener(toRender);
    canvas.addKeyListener(toRender);
    canvas.addMouseListener(toRender);
    canvas.addMouseMotionListener(toRender);
    canvas.addMouseWheelListener(toRender);

    final Frame frame = new Frame(toRender.getTitle());
    frame.add(canvas);
    frame.setSize(dx + 16, dy + 38);
    frame.setVisible(true);

    new Thread() {
      @Override
      public void run() {
        frame.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }
        });
        canvas.requestFocus();
        while (true) {
          canvas.display();
        }
      }
    }.start();
    return frame;
  }
}
