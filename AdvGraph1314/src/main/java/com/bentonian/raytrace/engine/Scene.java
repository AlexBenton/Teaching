package com.bentonian.raytrace.engine;

import java.util.ArrayList;
import java.util.List;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.scene.PrimitiveCollection;

public class Scene extends PrimitiveCollection {
  
  private List<M3d> lights;
  
  public Scene() {
    lights = new ArrayList<M3d>();
  }
  
  public List<M3d> getLights() {
    return lights;
  }
  
  public void addLight(M3d light) {
    lights.add(light);
  }
  
  public void removeLight(M3d light) {
    lights.remove(light);
  }

  public void clearLights() {
    lights.clear();
  }
}
