package com.bentonian.framework.mesh.metaballs;

import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.math.M3d;

public class MetaBall extends M3d implements Force, HasColor {
  
  double strength;
  M3d color;
  
  public MetaBall(double x, double y, double z, double strength, M3d color) {
    super(x,y,z);
    this.strength = strength;
    this.color = color;
  }
  
  @Override
  public double F(M3d v) {
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
  public M3d getColor() {
    return color;
  }
}
