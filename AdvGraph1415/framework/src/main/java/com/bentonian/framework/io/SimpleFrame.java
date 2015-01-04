package com.bentonian.framework.io;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SimpleFrame extends Frame {
  
  private int innerWidth, innerHeight, innerTop, innerLeft;

  public SimpleFrame(String title) {
    super(title);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent windowEvent) {
        onClose();
      }
    });
    addComponentListener(new ComponentListener() {
      @Override public void componentResized(ComponentEvent e) {
        onResized();
      }
      @Override public void componentHidden(ComponentEvent arg0) { }
      @Override public void componentMoved(ComponentEvent arg0) { }
      @Override public void componentShown(ComponentEvent arg0) { }
    });
    addKeyListener(new KeyListener() {
      @Override public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
          onClose();
        }
      }
      @Override public void keyReleased(KeyEvent keyEvent) { }
      @Override public void keyTyped(KeyEvent keyEvent) { }
    });
  }

  public void setInnerSize(int dx, int dy) {
    Panel panel = new Panel();
    add(panel);
    panel.setPreferredSize(new Dimension(dx, dy));
    pack();
    innerWidth = dx;
    innerHeight = dy;
    innerLeft = panel.getX();
    innerTop = panel.getY();
    remove(panel);
  }

  protected void onClose() {
  }

  public int getInnerLeft() {
    return innerLeft;
  }

  public int getInnerTop() {
    return innerTop;
  }

  public int getInnerWidth() {
    return innerWidth;
  }

  public int getInnerHeight() {
    return innerHeight;
  }

  /**
   * Override the default behavior of clearing the frame before repainting
   */
  @Override
  public void update(Graphics g){
    paint(g);
  }

  public void onResized() {
    Insets insets = getInsets();
    innerWidth = getWidth() - (insets.left + insets.right);
    innerHeight = getHeight() - (insets.top + insets.bottom);
    repaint();
  }
}
