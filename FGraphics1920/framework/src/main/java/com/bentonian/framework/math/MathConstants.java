package com.bentonian.framework.math;

public class MathConstants {

  public static final double EPSILON = 0.000001;
  
  public static final Vec3 ORIGIN = new Vec3(0);
  public static final Vec3 X_AXIS = new Vec3(1, 0, 0);
  public static final Vec3 Y_AXIS = new Vec3(0, 1, 0);
  public static final Vec3 Z_AXIS = new Vec3(0, 0, 1);
  public static final Vec3 NEGATIVE_X_AXIS = new Vec3(-1, 0, 0);
  public static final Vec3 NEGATIVE_Y_AXIS = new Vec3(0, -1, 0);
  public static final Vec3 NEGATIVE_Z_AXIS = new Vec3(0, 0, -1);

  public static final double[][] CORNERS_OF_A_SQUARE = {
    { -1, 1, 0, },
    { -1,-1, 0, },
    {  1,-1, 0, },
    {  1, 1, 0, },
  };

  public static final double[][] CORNERS_OF_A_CUBE = {
    { -1, -1, -1, },
    { -1, -1, 1, },
    { -1, 1, -1, },
    { -1, 1, 1, },
    { 1, -1, -1, },
    { 1, -1, 1, },
    { 1, 1, -1, },
    { 1, 1, 1, },
  };

  public static final int[][] INDICES_OF_A_CUBE = {
    { 0, 1, 3, 2, },
    { 2, 3, 7, 6, },
    { 4, 6, 7, 5, },
    { 0, 4, 5, 1, },
    { 1, 5, 7, 3, },
    { 0, 2, 6, 4, },
  };
}
