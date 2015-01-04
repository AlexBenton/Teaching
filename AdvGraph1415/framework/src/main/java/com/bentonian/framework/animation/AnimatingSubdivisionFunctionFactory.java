package com.bentonian.framework.animation;

import com.bentonian.framework.mesh.Edge;
import com.bentonian.framework.mesh.Face;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.mesh.subdivision.CatmullClark;
import com.bentonian.framework.mesh.subdivision.DooSabin;
import com.bentonian.framework.mesh.subdivision.Loop;

public class AnimatingSubdivisionFunctionFactory {
  
  public static DooSabin buildDooSabin(final AnimatedMeshPrimitive animationTarget) {
    return new DooSabin() {
      @Override
      protected Vertex vertexRule(Face face, Vertex vertex) {
        Vertex to = super.vertexRule(face, vertex);
        return animationTarget.addVertexAnimation(to, vertex, to);
      }

      @Override
      protected Vertex boundaryRule(Vertex near, Vertex far) {
        Vertex to = super.boundaryRule(near, far);
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
      protected Vertex vertexRule(Vertex from) {
        Vertex to = super.vertexRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      protected Vertex edgeRule(Edge from) {
        Vertex to = super.edgeRule(from);
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
      protected Vertex vertexRule(Vertex from) {
        Vertex to = super.vertexRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      protected Vertex edgeRule(Edge from) {
        Vertex to = super.edgeRule(from);
        return animationTarget.addVertexAnimation(to, from, to);
      }

      @Override
      protected Vertex faceRule(Face from) {
        Vertex to = super.faceRule(from);
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
