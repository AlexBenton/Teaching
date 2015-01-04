package com.bentonian.framework.scene;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.ui.GLRenderingContext;

public abstract class Primitive extends Transformable {

  public void render(GLRenderingContext glCanvas) {
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
  public Primitive translate(M3d v) {
    super.translate(v);
    return this;
  }

  @Override
  public Primitive rotate(M3d axis, double d) {
    super.rotate(axis, d);
    return this;
  }

  @Override
  public Primitive scale(M3d v) {
    super.scale(v);
    return this;
  }

  protected abstract void renderLocal(GLRenderingContext glCanvas);
}
