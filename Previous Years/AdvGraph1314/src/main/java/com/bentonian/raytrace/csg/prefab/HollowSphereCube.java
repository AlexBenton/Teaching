package com.bentonian.raytrace.csg.prefab;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.raytrace.csg.Difference;
import com.bentonian.raytrace.csg.Intersection;
import com.bentonian.raytrace.csg.Union;

public class HollowSphereCube extends Intersection {

  private static final Primitive A = new Sphere(new M3d(1, 1, 1)).scale(new M3d(200, 0.55, 0.55));
  private static final Primitive B = new Sphere(new M3d(1, 1, 1)).scale(new M3d(0.55, 200, 0.55));
  private static final Primitive C = new Sphere(new M3d(1, 1, 1)).scale(new M3d(0.55, 0.55, 200));
  private static final Primitive AXES = new Union(A, new Union(B, C));
  private static final Primitive D = new Cube(new M3d(0.2, 0.5, 0.8)).scale(new M3d(0.7, 0.7, 0.7));
  private static final Primitive E1 = new Sphere(new M3d(1, 1, 1));
  private static final Primitive E2 = new Sphere(new M3d(1, 1, 1)).scale(new M3d(0.85, 0.85, 0.85));
  
  public HollowSphereCube() {
    super(
        new Difference(D, AXES), 
        new Difference(E1, E2));
  }
}
