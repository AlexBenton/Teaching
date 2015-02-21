package com.bentonian.framework.ui;

import com.bentonian.framework.math.Ray;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.scene.IsRayTraceable;

public interface MouseEventHandler extends IsRayTraceable {

  public void onMouseDown(Camera camera, Ray ray);

  public void onMouseOver(Camera camera, Ray ray);

  public void onMouseOut(Camera camera, Ray ray);

  public void onMouseDrag(Camera camera, Ray ray);

  public void onMouseUp(Camera camera, Ray ray);
}