package com.bentonian.gldemos.hierarchy;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLVertexData;


public class HierarchyDemo extends DemoApp {

  private static final Primitive SPHERE = new Sphere(10, 8)
      .scale(new M3d(0.25, 0.25, 0.25));
  private static final GLVertexData LINES = GLVertexData.beginLineSegments()
      .color(new M3d(1, 0, 0))
      .vertex(new M3d(0, 0, 0))
      .vertex(new M3d(1, -0.75, 0))
      .vertex(new M3d(0, 0, 0))
      .vertex(new M3d(-1, -0.75, 0));
  private static final M4x4 SHRINK = M4x4.scaleMatrix(new M3d(0.75, 0.75, 0.75));
  private static final M4x4 LEFT = M4x4.translationMatrix(new M3d(-1, -0.75, 0));
  private static final M4x4 RIGHT = M4x4.translationMatrix(new M3d(1, -0.75, 0));

  private int numLevels = 1;
  private int tick = 0;
  private boolean paused = false;

  public HierarchyDemo() {
    super("Hierarchical model");
    setCameraDistance(5);
  }
  
  @Override
  public void initGl() {
    super.initGl();
    GL11.glLineWidth(4);
  }

  void renderLevel(int level) {
    float t = ((float) tick) / 25.0f;

    push(M4x4.rotationMatrix(new M3d(0, 1, 0), t));
    SPHERE.render(this);
    if (level > 0) {
      push(SHRINK);
      LINES.render(this);

      push(LEFT);
      renderLevel(level-1);
      pop();

      push(RIGHT);
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
  public void onKeyDown(int key) {
    switch (key) {

    case Keyboard.KEY_EQUALS:
      numLevels = Math.min(numLevels + 1, 12);
      break;

    case Keyboard.KEY_MINUS:
      numLevels = Math.max(numLevels - 1, 0);
      break;

    case Keyboard.KEY_SPACE:
      paused = !paused;
      break;

    default:
      super.onKeyDown(key);
    }
  }

  public static void main(String[] args) {
    new HierarchyDemo().run();
  }
}
