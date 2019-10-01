package com.bentonian.framework.raytrace.csg.prefab;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.mesh.primitive.Torus;
import com.bentonian.framework.raytrace.csg.Difference;
import com.bentonian.framework.raytrace.csg.Intersection;
import com.bentonian.framework.raytrace.csg.Union;
import com.bentonian.framework.scene.Primitive;

public class CarvedSphere extends Union {
  
  private static final Primitive SPHERE = 
      new Sphere().setColor(new Vec3(0.2, 0.2, 0.8));
  private static final Primitive TORUS = 
      new Torus().setColor(new Vec3(0.8,0.5,0.2)).scale(new Vec3(0.75,0.75,0.75));

  public CarvedSphere() {
    super(
        new Difference(SPHERE, TORUS),
        new Intersection(SPHERE, TORUS).scale(new Vec3(1.5,0.5,1.5)));
  }
}
