package com.bentonian.framework.raytrace.csg.prefab;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.raytrace.csg.Difference;
import com.bentonian.framework.raytrace.csg.Intersection;
import com.bentonian.framework.raytrace.csg.Union;
import com.bentonian.framework.scene.Primitive;

public class HollowSphereCube extends Intersection {

  private static final Primitive A = new Sphere().scale(new M3d(200, 0.55, 0.55));
  private static final Primitive B = new Sphere().scale(new M3d(0.55, 200, 0.55));
  private static final Primitive C = new Sphere().scale(new M3d(0.55, 0.55, 200));
  private static final Primitive AXES = new Union(A, new Union(B, C));
  private static final Primitive D = new Cube().setColor(new M3d(0.2, 0.5, 0.8)).scale(new M3d(0.7, 0.7, 0.7));
  private static final Primitive E1 = new Sphere();
  private static final Primitive E2 = new Sphere().scale(new M3d(0.85, 0.85, 0.85));
  
  public HollowSphereCube() {
    super(
        new Difference(D, AXES), 
        new Difference(E1, E2));
  }
}
