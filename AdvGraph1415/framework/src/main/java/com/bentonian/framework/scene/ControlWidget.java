package com.bentonian.framework.scene;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.ui.MouseEventHandler;

public class ControlWidget extends Sphere implements MouseEventHandler {

  private static final M3d BLUE = new M3d(0, 0, 0.8);
  private static final M3d WHITE = new M3d(1, 1, 1);
  private static final M3d GRAY = new M3d(0.6, 0.6, 0.6);

  protected boolean isHighlighted = false;
  protected boolean isSelected = false;

  private M3d dragPlaneNormal;
  private M3d dragPlaneOrigin;
  private M3d intersectionOffset;

  public ControlWidget() {
    setColor(GRAY);
  }

  @Override
  public void onMouseDown(Camera camera, Ray ray) {
    setSelected(true);
    dragPlaneNormal = camera.getDirection().times(-1);
    Double t = ray.intersectPlane(getPosition(), dragPlaneNormal);
    if (t != null) {
      dragPlaneOrigin = ray.at(t);
      intersectionOffset = getPosition().minus(dragPlaneOrigin);
    }
  }

  @Override
  public void onMouseOver(Camera camera, Ray ray) {
    setHighlighted(true);
  }

  @Override
  public void onMouseOut(Camera camera, Ray ray) {
    setHighlighted(false);
  }

  @Override
  public void onMouseDrag(Camera camera, Ray ray) {
    Double t = ray.intersectPlane(dragPlaneOrigin, dragPlaneNormal);
    if (t != null && isSelected) {
      M3d pos = ray.at(t);
      translate(pos.plus(intersectionOffset).minus(getPosition()));
    }
  }

  @Override
  public void onMouseUp(Camera camera, Ray ray) {
    setSelected(false);
    setHighlighted(isHit(camera, ray));
  }

  public void setSelected(boolean isSelected) {
    if (this.isSelected != isSelected) {
      this.isSelected = isSelected;
      setColor(isSelected ? BLUE : isHighlighted ? WHITE : GRAY);
      dispose();
    }
  }

  public void setHighlighted(boolean isHighlighted) {
    if (this.isHighlighted != isHighlighted) {
      this.isHighlighted = isHighlighted;
      setColor(isSelected ? BLUE : isHighlighted ? WHITE : GRAY);
      dispose();
    }
  }

  private boolean isHit(Camera camera, Ray ray) {
    return !RayTracerEngine.traceScene(this, ray).isEmpty();
  }
}
