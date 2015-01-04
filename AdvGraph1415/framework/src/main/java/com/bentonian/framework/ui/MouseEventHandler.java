package com.bentonian.framework.ui;

import com.bentonian.framework.math.Ray;
import com.bentonian.framework.scene.Camera;

public interface MouseEventHandler {

  public boolean onMouseDown(Camera camera, Ray ray);

  public boolean onMouseMove(Camera camera, Ray ray);

  public boolean onMouseDrag(Camera camera, Ray ray);

  public boolean onMouseUp(Camera camera, Ray ray);
}