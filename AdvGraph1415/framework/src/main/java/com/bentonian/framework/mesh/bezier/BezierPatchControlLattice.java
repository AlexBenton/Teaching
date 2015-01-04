package com.bentonian.framework.mesh.bezier;

import java.util.Map;
import java.util.Set;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.scene.ControlWidget;
import com.bentonian.framework.scene.PrimitiveCollection;
import com.bentonian.framework.ui.GLWindowedCanvas;
import com.bentonian.framework.ui.MouseEventHandler;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BezierPatchControlLattice extends PrimitiveCollection implements MouseEventHandler {

  private final BezierPatch patch;
  private final LatticeControlWidget[][] controls = new LatticeControlWidget[4][4];
  private final BezierPatchControlLatticeWireframe latticeWireframe;

  private LatticeControlWidget hover;
  private Set<LatticeControlWidget> selections;
  private Map<LatticeControlWidget, M3d> selectionDragPlaneOffset;
  private M3d dragPlaneOrigin;
  private M3d dragPlaneNormal;
  private boolean captureMouse;
  private boolean clearSelectionOnMouseUp;

  public BezierPatchControlLattice(BezierPatch patch) {
    this.patch = patch;
    this.latticeWireframe = new BezierPatchControlLatticeWireframe(patch); 
    this.selections = Sets.newHashSet();
    this.selectionDragPlaneOffset = Maps.newHashMap();

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        controls[i][j] = new LatticeControlWidget();
        controls[i][j].i = i;
        controls[i][j].j = j;
        controls[i][j].translate(patch.getControlPoint(i, j));
        add(controls[i][j]);
      }
    }
    add(latticeWireframe);
  }

  @Override
  public boolean onMouseDown(Camera camera, Ray ray) {
    LatticeControlWidget widget = pick(camera, ray);
    if (widget != null) {
      if (!GLWindowedCanvas.isControlDown()) {
        clearSelection();
      }
      selections.add(widget);
      widget.setSelected(true);
      dragPlaneNormal = camera.getDirection().times(-1);
      Double t = ray.intersectPlane(widget.getPosition(), dragPlaneNormal);
      if (t != null) {
        dragPlaneOrigin = ray.at(t);
        for (LatticeControlWidget control : selections) {
          selectionDragPlaneOffset.put(control, control.getPosition().minus(dragPlaneOrigin));
        }
        captureMouse = true;
      }
      return true;
    } else {
      captureMouse = false;
      clearSelectionOnMouseUp = true;
      return false;
    }
  }

  @Override
  public boolean onMouseMove(Camera camera, Ray ray) {
    LatticeControlWidget widget = pick(camera, ray);
    if (widget != null) {
      if (hover != widget) {
        if (hover != null) {
          hover.setHighlighted(false);
        }
        hover = widget;
        hover.setHighlighted(true);
      }
    } else {
      clearHover();
    }
    return false;
  }

  @Override
  public boolean onMouseDrag(Camera camera, Ray ray) {
    clearSelectionOnMouseUp = false;
    if (captureMouse) {
      Double t = ray.intersectPlane(dragPlaneOrigin, dragPlaneNormal);
      if (t != null && !selections.isEmpty()) {
        for (LatticeControlWidget control : selections) {
          M3d pos = selectionDragPlaneOffset.get(control).plus(ray.at(t));
          control.translate(pos.minus(control.getPosition()));
          patch.getControlPoint(control.i, control.j).set(pos);
        }
        patch.dispose();
        latticeWireframe.dispose();
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean onMouseUp(Camera camera, Ray ray) {
    if (clearSelectionOnMouseUp) {
      clearSelection();
    }
    if (captureMouse) {
      captureMouse = false;
      return true;
    } else {
      return false;
    }
  }

  private LatticeControlWidget pick(Camera camera, Ray ray) {
    RayIntersections hits = RayTracerEngine.traceScene(this, ray);
    return hits.isEmpty() ? null : (LatticeControlWidget) hits.getNearest().primitive;
  }
  
  private void clearHover() {
    if (hover != null) {
      hover.setHighlighted(false);
      hover = null;
    }
  }
  
  private void clearSelection() {
    for (LatticeControlWidget control : selections) {
      control.setSelected(false);
    }
    selections.clear();
    selectionDragPlaneOffset.clear();
  }
  
  private static class LatticeControlWidget extends ControlWidget {
    int i, j;
  }
}
