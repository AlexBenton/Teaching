package com.bentonian.framework.math;

public class MathConstants {

  public static final double EPSILON = 0.000001;

  public static final M3d[] CORNERS_OF_A_SQUARE =
    { new M3d(-1, 1, 0), new M3d(-1,-1, 0), new M3d( 1,-1, 0), new M3d( 1, 1, 0), };

  public static final M3d[][] FACES_OF_A_CUBE = {
    { new M3d(-1, 1, 1), new M3d(-1,-1, 1), new M3d( 1,-1, 1), new M3d( 1, 1, 1), },
    { new M3d(-1, 1,-1), new M3d(-1,-1,-1), new M3d(-1,-1, 1), new M3d(-1, 1, 1), },
    { new M3d(-1, 1,-1), new M3d(-1, 1, 1), new M3d( 1, 1, 1), new M3d( 1, 1,-1), },
    { new M3d(-1,-1, 1), new M3d(-1,-1,-1), new M3d( 1,-1,-1), new M3d( 1,-1, 1), },
    { new M3d( 1, 1, 1), new M3d( 1,-1, 1), new M3d( 1,-1,-1), new M3d( 1, 1,-1), },
    { new M3d( 1, 1,-1), new M3d( 1,-1,-1), new M3d(-1,-1,-1), new M3d(-1, 1,-1), },
  };

  public static final M3d[] NORMALS_OF_A_CUBE = {
    new M3d( 0,  0,  1),
    new M3d(-1,  0,  0),
    new M3d( 0,  1,  0),
    new M3d( 0, -1,  0),
    new M3d( 1,  0,  0),
    new M3d( 0,  0, -1),
  };
}
