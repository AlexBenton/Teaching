package com.bentonian.framework.raytrace.engine;

import java.util.List;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.scene.PrimitiveCollection;
import com.google.common.collect.Lists;

public class Scene extends PrimitiveCollection {
  
  private List<M3d> lights = Lists.newArrayList();
  
  public Scene() {
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
