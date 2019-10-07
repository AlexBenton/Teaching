package com.bentonian.framework.scene;

import static com.bentonian.framework.material.Colors.BLUE;
import static com.bentonian.framework.material.Colors.GRAY;
import static com.bentonian.framework.material.Colors.WHITE;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.ui.MouseEventHandler;

public class ControlWidget extends Sphere implements MouseEventHandler {

  protected boolean isHighlighted = false;
  protected boolean isSelected = false;

  private Vec3 dragPlaneNormal;
  private Vec3 dragPlaneOrigin;
  private Vec3 intersectionOffset;

  public ControlWidget() {
    super(40, 20);
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
      Vec3 pos = ray.at(t);
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
