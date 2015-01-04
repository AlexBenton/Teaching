package com.bentonian.raytrace.csg.prefab;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.mesh.primitive.Torus;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.raytrace.csg.Difference;
import com.bentonian.raytrace.csg.Intersection;
import com.bentonian.raytrace.csg.Union;

public class CarvedSphere extends Union {
  
  private static final Primitive SPHERE = new Sphere(new M3d(0.2,0.5,0.8)).setReflectivity(0.75);
  private static final Primitive TORUS = 
      new Torus(new M3d(0.8,0.5,0.2)).scale(new M3d(0.75,0.75,0.75));

  public CarvedSphere() {
    super(
        new Difference(SPHERE, TORUS),
        new Intersection(SPHERE, TORUS).scale(new M3d(1.5,0.5,1.5)));
  }
}
