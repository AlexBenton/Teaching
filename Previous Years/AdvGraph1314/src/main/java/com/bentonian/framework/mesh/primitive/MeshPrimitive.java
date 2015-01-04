package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersection;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.ui.GLRenderingContext;
import com.bentonian.framework.ui.GLVertexData;

public class MeshPrimitive extends CompiledPrimitive implements IsRayTraceable {

  private final Mesh mesh;

  private MeshPrimitiveRayTracingAccelerator accelerator;

  public MeshPrimitive(Mesh mesh) {
    super(GLVertexData.Mode.TRIANGLES);
    this.mesh = mesh;
    this.accelerator = null;
  }

  public Mesh getMesh() {
    return mesh;
  }

  public void enableRayTracingAccelerator() {
    this.accelerator = new MeshPrimitiveRayTracingAccelerator(mesh);
  }

  public void disableRayTracingAccelerator() {
    this.accelerator = null;
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    RayIntersections hits = new RayIntersections();

    if (accelerator == null) {
      for (Face face : mesh) {
        traceRayToFace(ray, hits, face);
      }
    } else if (accelerator.isHitByRay(ray)) {
      for (Face face : accelerator.getFacesAlongRay(ray)) {
        traceRayToFace(ray, hits, face);
      }
    }
    return hits;
  }

  public void traceRayToFace(Ray ray, RayIntersections hits, Face face) {
    for (int i = 0; i < face.size() - 2; i++) {
      Vertex A = face.get(0);
      Vertex B = face.get(i + 1);
      Vertex C = face.get(i + 2);
      Double t = ray.intersectsTriangle(A, B, C, face.getNormal());
      if (t != null) {
        M3d pt = ray.at(t);
        hits.add(new RayIntersection(t, pt, getNormal(A, B, C, pt), getMaterial(pt)));
      }
    }
  }

  protected M3d getNormal(Vertex A, Vertex B, Vertex C, M3d pt) {
    double barycentricWeightA = B.minus(pt).cross(C.minus(pt)).length();
    double barycentricWeightB = C.minus(pt).cross(A.minus(pt)).length();
    double barycentricWeightC = A.minus(pt).cross(B.minus(pt)).length();
    return A.getNormal().times(barycentricWeightA)
        .plus(B.getNormal().times(barycentricWeightB))
        .plus(C.getNormal().times(barycentricWeightC))
        .normalized();
  }

  protected Material getMaterial(M3d pt) {
    return getMaterial();
  }

  @Override
  protected void renderLocal(GLRenderingContext glCanvas) {
    if (!isCompiled()) {
      for (Face face : mesh) {
        normal(face.getNormal());
        for (int i = 0; i < face.size() - 2; i++) {
          addTriangle(glCanvas, face.get(0), face.get(i + 1), face.get(i + 2));
        }
      }
    }
    super.renderLocal(glCanvas);
  }
  
  protected void addTriangle(GLRenderingContext glCanvas, Vertex A, Vertex B, Vertex C) {
    vertex(A);
    vertex(B);
    vertex(C);
  }
}
