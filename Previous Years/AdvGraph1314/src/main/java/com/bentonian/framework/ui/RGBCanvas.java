package com.bentonian.framework.ui;

import com.bentonian.framework.math.M3d;

/**
 * Interface for 2D drawing surface adapter layer
 * 
 * @author Alex Benton
 */
public interface RGBCanvas {

  public int getWidth();
  public int getHeight();
  public void fill(double x, double y, double dx, double dy, M3d color);
  public void putPixel(int x, int y, M3d color);
}
