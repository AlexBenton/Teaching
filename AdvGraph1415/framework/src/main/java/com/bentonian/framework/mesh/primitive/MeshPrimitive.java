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
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLVertexData;

public class MeshPrimitive extends CompiledPrimitive implements IsRayTraceable {

  public enum RenderStyle {
    NORMALS_BY_FACE,
    NORMALS_BY_VERTEX
  }

  private final Mesh mesh;

  private MeshPrimitiveFeatureAccelerator featuresAccelerator;
  private MeshPrimitiveRayTracingAccelerator rayTracingAccelerator;
  private RenderStyle renderStyle = RenderStyle.NORMALS_BY_FACE;

  public MeshPrimitive(Mesh mesh) {
    super(GLVertexData.Mode.TRIANGLES);
    this.mesh = mesh;
  }

  public Mesh getMesh() {
    return mesh;
  }

  public void setRenderStyle(RenderStyle renderStyle) {
    this.renderStyle = renderStyle;
  }

  public RenderStyle getRenderStyle() {
    return renderStyle;
  }

  public void enableRayTracingAccelerator() {
    rayTracingAccelerator = new MeshPrimitiveRayTracingAccelerator(mesh);
  }

  public void disableRayTracingAccelerator() {
    rayTracingAccelerator = null;
  }

  public MeshPrimitiveFeatureAccelerator getFeaturesAccelerator() {
    if (featuresAccelerator == null) {
      featuresAccelerator = new MeshPrimitiveFeatureAccelerator(mesh);
    }
    return featuresAccelerator;
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    RayIntersections hits = new RayIntersections();

    if (rayTracingAccelerator == null) {
      for (Face face : mesh) {
        traceRayToFace(ray, hits, face);
      }
    } else if (rayTracingAccelerator.isHitByRay(ray)) {
      for (Face face : rayTracingAccelerator.getFacesAlongRay(ray)) {
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
        M3d normal = (renderStyle == RenderStyle.NORMALS_BY_FACE)
            ? face.getNormal() : getNormalFromBarycentricWeights(A, B, C, pt);
        Material material = getMaterial(pt);
        hits.add(new RayIntersection(this, t, pt, normal, material));
      }
    }
  }

  private M3d getNormalFromBarycentricWeights(Vertex A, Vertex B, Vertex C, M3d pt) {
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
  protected void renderLocal(GLCanvas glCanvas) {
    if (featuresAccelerator != null) {
      featuresAccelerator.render(glCanvas);
    }
    if (!isCompiled()) {
      for (Face face : mesh) {
        normal(face.getNormal());
        renderFace(face);
      }
    }
    super.renderLocal(glCanvas);
  }

  @Override
  public void dispose() {
    if (featuresAccelerator != null) {
      featuresAccelerator.dispose();
    }
    super.dispose();
    // TODO(me) Reset raytracing accelerator here?
  }

  protected void renderFace(Face face) {
    if (renderStyle == RenderStyle.NORMALS_BY_FACE) {
      normal(face.getNormal());
    }
    for (int i = 0; i < face.size() - 2; i++) {
      renderVertex(face, 0);
      renderVertex(face, i + 1);
      renderVertex(face, i + 2);
    }
  }

  protected void renderVertex(Face face, int index) {
    if (renderStyle == RenderStyle.NORMALS_BY_VERTEX) {
      normal(face.get(index).getNormal());
    }
    color(getMaterial(face.get(index)).getColor());
    vertex(face.get(index));
  }
}
