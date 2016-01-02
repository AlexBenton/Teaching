package com.bentonian.framework.math;

public class MathConstants {

  public static final double EPSILON = 0.000001;

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
