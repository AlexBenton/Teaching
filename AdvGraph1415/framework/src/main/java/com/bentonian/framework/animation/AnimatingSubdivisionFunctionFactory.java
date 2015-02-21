package com.bentonian.framework.animation;

import com.bentonian.framework.mesh.MeshEdge;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshVertex;
import com.bentonian.framework.mesh.subdivision.CatmullClark;
import com.bentonian.framework.mesh.subdivision.DooSabin;
import com.bentonian.framework.mesh.subdivision.Loop;

public class AnimatingSubdivisionFunctionFactory {
  
  public static DooSabin buildDooSabin(final AnimatedMeshPrimitive animationTarget) {
    return new DooSabin() {
      @Override
      protected MeshVertex vertexRule(MeshFace face, MeshVertex vertex) {
        MeshVertex to = super.vertexRule(face, vertex);
        return animationTarget.addVertexAnimation(to, vertex, to);
      }

      @Override
      protected MeshVertex boundaryRule(MeshVertex near, MeshVertex far) {
        MeshVertex to = super.boundaryRule(near, far);
        return animationTarget.addVertexAnimation(to, near, to);
      }

      @Override
      public Mesh apply(Mesh mesh) {
        Mesh subdivided = super.apply(mesh);
        animationTarget.copyFaces(subdivided);
        return subdivided;
      }
    };
  }

  public static Loop buildLoop(final AnimatedMeshPrimitive animationTarget) {
    return new Loop() {
      @Override
      protected MeshVertex vertexRule(MeshVertex from) {
        MeshVertex to = super.vertexRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      protected MeshVertex edgeRule(MeshEdge from) {
        MeshVertex to = super.edgeRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      public Mesh apply(Mesh mesh) {
        Mesh subdivided = super.apply(mesh);
        animationTarget.copyFaces(subdivided);
        return subdivided;
      }
    };
  }
  
  public static CatmullClark buildCatmullClark(final AnimatedMeshPrimitive animationTarget) {
    return new CatmullClark() {
      @Override
      protected MeshVertex vertexRule(MeshVertex from) {
        MeshVertex to = super.vertexRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      protected MeshVertex edgeRule(MeshEdge from) {
        MeshVertex to = super.edgeRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      protected MeshVertex faceRule(MeshFace from) {
        MeshVertex to = super.faceRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      public Mesh apply(Mesh mesh) {
        Mesh subdivided = super.apply(mesh);
        animationTarget.copyFaces(subdivided);
        return subdivided;
      }
    };
  }
}
