package com.bentonian.framework.material;

import com.bentonian.framework.math.M3d;

public class Colors {

  public static final M3d GRAY = new M3d(0.6, 0.6, 0.6);
  public static final M3d BLACK = new M3d(0, 0, 0);
  public static final M3d WHITE = new M3d(1, 1, 1);
  public static final M3d RED = new M3d(203, 65, 84).times(1 / 255.0);
  public static final M3d BLUE = new M3d(84, 65, 203).times(1 / 255.0);
  public static final M3d SKY_BLUE = new M3d(135, 206, 250).times(1.0 / 255.0);
  public static final M3d GREEN = new M3d(65, 203, 84).times(1 / 255.0);
  public static final M3d ORANGE = new M3d(0xFF, 0xA5, 0x00).times(1 / 255.0);
}
