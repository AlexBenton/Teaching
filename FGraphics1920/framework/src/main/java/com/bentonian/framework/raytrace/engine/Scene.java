package com.bentonian.framework.raytrace.engine;

import java.util.List;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.scene.PrimitiveCollection;
import com.google.common.collect.Lists;

public class Scene extends PrimitiveCollection {
  
  private List<Vec3> lights = Lists.newArrayList();
  
  public Scene() {
  }
  
  public List<Vec3> getLights() {
    return lights;
  }
  
  public void addLight(Vec3 light) {
    lights.add(light);
  }
  
  public void removeLight(Vec3 light) {
    lights.remove(light);
  }

  public void clearLights() {
    lights.clear();
  }
}
