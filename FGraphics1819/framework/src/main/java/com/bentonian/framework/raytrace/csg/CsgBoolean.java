package com.bentonian.framework.raytrace.csg;

import java.util.Set;

import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersection;
import com.bentonian.framework.math.RayIntersectionList;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.ui.GLCanvas;

public abstract class CsgBoolean extends Primitive implements IsRayTraceable {

  private Primitive A;
  private Primitive B;

  public CsgBoolean(Primitive A, Primitive B) {
    this.A = A;
    this.B = B;
  }

  @Override
  public RayIntersections traceLocal(Ray ray) {
    RayIntersectionList hitsA = RayTracerEngine.traceScene(A, ray).sorted();
    RayIntersectionList hitsB = RayTracerEngine.traceScene(B, ray).sorted();
    boolean inA = hitsA.isEmpty() ? false : ((hitsA.size() % 2) == 1);
    boolean inB = hitsB.isEmpty() ? false : ((hitsB.size() % 2) == 1);
    return mergeHitLists(ray, CsgState.get(inA, inB), hitsA, hitsB);
  }

  private RayIntersections mergeHitLists(Ray ray,
      CsgState state, RayIntersectionList hitsA, RayIntersectionList hitsB) {
    RayIntersections merged = new RayIntersections();
    CsgState newState;
    RayIntersection next;
    
    while (!hitsA.isEmpty() || !hitsB.isEmpty()) {
      if (hitsB.isEmpty() || (!hitsA.isEmpty() && (hitsA.peek() < hitsB.peek()))) {
        newState = state.toggleA();
        next = popA(hitsA);
      } else {
        newState = state.toggleB();
        next = popB(hitsB);
      }
      if (interested(state, newState)) {
        merged.add(next);
      }
      state = newState;
    }
    return merged;
  }

  private boolean interested(CsgState from, CsgState to) {
    Set<CsgState> statesOfInterest = getStatesOfInterest();
    return statesOfInterest.contains(from) != statesOfInterest.contains(to);
  }

  protected abstract Set<CsgState> getStatesOfInterest();

  protected RayIntersection popA(RayIntersectionList hitsA) {
    return hitsA.pop();
  }

  protected RayIntersection popB(RayIntersectionList hitsB) {
    return hitsB.pop();
  }

  @Override
  public void render(GLCanvas glCanvas) {
    glCanvas.push(getLocalToParent());
    A.render(glCanvas);
    B.render(glCanvas);
    glCanvas.pop();
  }

  @Override
  protected void renderLocal(GLCanvas glCanvas) {
  }
}