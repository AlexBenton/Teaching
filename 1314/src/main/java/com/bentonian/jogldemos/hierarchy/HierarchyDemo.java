package com.bentonian.jogldemos.hierarchy;

import java.awt.event.KeyEvent;

import javax.media.opengl.GLAutoDrawable;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.ui.GLVertexData;
import com.bentonian.jogldemos.internals.JoglDemo;
import com.bentonian.jogldemos.internals.JoglDemoContainer;


public class HierarchyDemo extends JoglDemo {

  private final Primitive sphere = new Sphere(10, 8)
      .scale(new M3d(0.25, 0.25, 0.25));
  private final GLVertexData lines = GLVertexData.beginLineSegments()
      .color(new M3d(1, 0, 0))
      .vertex(new M3d(0, 0, 0))
      .vertex(new M3d(1, -0.75, 0))
      .vertex(new M3d(0, 0, 0))
      .vertex(new M3d(-1, -0.75, 0));

  private int numLevels = 1;
  private int tick = 0;
  private boolean paused = false;

  public HierarchyDemo() {
    super("Hierarchical model");
  }

  @Override
  public void init(GLAutoDrawable glDrawable) {
    super.init(glDrawable);
    gl.glLineWidth(3);
    getCamera().setIdentity().translate(new M3d(0, 0, 5));
  }

  void renderLevel(int level) {
    float t = ((float) tick) / 25.0f;

    push(M4x4.rotationMatrix(new M3d(0, 1, 0), t));
    sphere.render(this);
    if (level > 0) {
      push(M4x4.scaleMatrix(new M3d(0.75, 0.75, 0.75)));
      lines.render(this);

      push(M4x4.translationMatrix(new M3d(1, -0.75, 0)));
      renderLevel(level-1);
      pop();

      push(M4x4.translationMatrix(new M3d(-1, -0.75, 0)));
      renderLevel(level-1);
      pop();

      pop();
    }
    pop();
  }

  @Override
  public void draw() {
    push(M4x4.translationMatrix(new M3d(0, 1, 0)));
    renderLevel(numLevels);
    if (!paused) {
      tick++;
    }
    pop();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyChar()) {

    case '=':
    case '+':
      numLevels = Math.min(numLevels + 1, 12);
      break;

    case '-':
    case '_':
      numLevels = Math.max(numLevels - 1, 0);
      break;

    case ' ':
      paused = !paused;
      break;

    default:
      super.keyPressed(e);
    }
  }

  public static void main(String[] args) {
    JoglDemoContainer.go(new HierarchyDemo());
  }
}
