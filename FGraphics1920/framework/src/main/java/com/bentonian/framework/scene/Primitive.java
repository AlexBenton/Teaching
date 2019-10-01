package com.bentonian.framework.scene;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.ui.GLCanvas;

public abstract class Primitive extends Transformable {

  public void render(GLCanvas glCanvas) {
    glCanvas.push(getLocalToParent());
    renderLocal(glCanvas);
    glCanvas.pop();
  }

  @Override
  public Primitive setIdentity() {
    super.setIdentity();
    return this;
  }

  @Override
  public Primitive translate(Vec3 v) {
    super.translate(v);
    return this;
  }

  @Override
  public Primitive rotate(Vec3 axis, double d) {
    super.rotate(axis, d);
    return this;
  }

  @Override
  public Primitive scale(Vec3 v) {
    super.scale(v);
    return this;
  }

  @Override
  public Primitive scale(double d) {
    super.scale(d);
    return this;
  }

  protected abstract void renderLocal(GLCanvas glCanvas);
}
