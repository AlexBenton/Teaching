package com.bentonian.helloshader;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;

/**
 * Sample code to demonstrate the basics of OpenGL.
 * Written for delivery to Advanced Graphics, Cambridge University, 2013-2014.
 * 
 * This class builds an AWT Frame, attaches a GLCanvas to it, and embeds
 * an GLEventListener instance within that.  This allows us to separate
 * our OpenGL code from our AWT code.
 *
 * @author Alex Benton
 */
public class HelloShaderAWTFrame {

  public static void wrap(final String title, final GLEventListener glEventListener) {
    new Thread() {
      @Override
      public void run() {
        Frame frame = new Frame(title);
        GLCanvas canvas = new GLCanvas();

        // Setup GL canvas
        frame.add(canvas);
        canvas.addGLEventListener(glEventListener);

        // Setup AWT frame
        frame.setSize(400, 400);
        frame.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(WindowEvent e) {
            System.exit(0);
          }
        });
        frame.setVisible(true);

        // Render loop
        while (true) {
          canvas.display();
        }
      }
    }.start();
  }
}
