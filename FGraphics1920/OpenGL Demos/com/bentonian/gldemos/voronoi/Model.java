package com.bentonian.gldemos.voronoi;

import com.bentonian.framework.io.OFFUtil;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.primitive.CompiledPrimitive;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.MeshPrimitive;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.mesh.primitive.Torus;
import com.bentonian.framework.scene.Primitive;

public enum Model {
  CUBE(new Cube().scale(new Vec3(1.5,1.9,1))),
  SPHERE(new Sphere()),
  TORUS(new Torus()),
  TEAPOT(new MeshPrimitive(OFFUtil.parseFile("teapot.off"))),
  BUNNY(new MeshPrimitive(OFFUtil.parseFile("bunny.off")).scale(new Vec3(2, 2, 2))),
  COW(new MeshPrimitive(OFFUtil.parseFile("cow.off")).scale(new Vec3(2, 2, 2))),
  ;

  private final Primitive geometry;

  private Model(Primitive geometry) {
    this.geometry = geometry;
  }

  public Model prev() {
    return values()[((ordinal() + values().length - 1) % values().length)];
  }

  public Model next() {
    return values()[((ordinal() + 1) % values().length)];
  }

  public void dispose() {
    if (geometry instanceof CompiledPrimitive) {
      ((CompiledPrimitive) geometry).dispose();
    }
  }
  
  public Primitive getGeometry() {
    return geometry;
  }
}
