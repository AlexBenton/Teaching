package com.bentonian.framework.scene;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Sphere;

public class ControlWidget extends Sphere {

  private static final M3d BLUE = new M3d(0, 0, 0.8);
  private static final M3d WHITE = new M3d(1, 1, 1);
  private static final M3d GRAY = new M3d(0.6, 0.6, 0.6);
  
  private boolean isHighlighted = false;
  private boolean isSelected = false;
  
  public ControlWidget() {
    super(10, 8);
    scale(0.05);
    setColor(GRAY);
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
}
