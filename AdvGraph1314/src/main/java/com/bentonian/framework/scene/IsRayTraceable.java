package com.bentonian.framework.scene;

import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;

public interface IsRayTraceable {

  public RayIntersections traceLocal(Ray ray);
}
