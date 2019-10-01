package com.bentonian.framework.mesh.implicits;

import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.math.Vec3;

public class MetaBall extends Vec3 implements Force, HasColor {
  
  double strength;
  Vec3 color;
  
  public MetaBall(double x, double y, double z, double strength, Vec3 color) {
    super(x,y,z);
    this.strength = strength;
    this.color = color;
  }
  
  @Override
  public double F(Vec3 v) {
    double r = this.minus(v).length();
    return wyvill(r, strength);
  }
   
  /**
   * The Wyvill Brothers' "soft object" function
   */
  public static double wyvill(double r, double strength) {
    double b = 5;
    
    if (r < b/3.0) {
      return strength * (1 - ((3 * r * r) / (b*b)));
    } else if (r >= b/3.0 && r < b) {
      return strength * 1.5 * (1-(r/b)) * (1-(r/b));
    } else {
      return 0;
    }
  }

  @Override
  public Vec3 getColor() {
    return color;
  }
}
