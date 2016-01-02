package com.bentonian.framework.scene;

import java.util.ArrayList;
import java.util.List;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;

/**
 * Pairing of M4x4s representing the cameraToWorld and worldToCamera transforms.
 *
 * @author Alex Benton
 */
public class Camera {

  private static final double FOV = 46;

  private final List<Transformable> stack;

  public Camera() {
    this.stack = new ArrayList<>();
    stack.add(new Transformable());
    translate(new M3d(0, 0, 1));
  }

  public Camera(Camera source) {
    this.stack = new ArrayList<>();
    for (Transformable t : source.stack) {
      stack.add(new Transformable(t));
    }
  }

  public M4x4 getLocalToParent() {
    return peek().getLocalToParent();
  }

  public void setLocalToParent(M4x4 T) {
    peek().setLocalToParent(T);
  }

  public M4x4 getParentToLocal() {
    return peek().getParentToLocal();
  }

  public Camera add(M4x4 T) {
    stack.add(new Transformable());
    return this;
  }

  public Camera push(M4x4 T) {
    stack.add(new Transformable(peek()));
    peek().setLocalToParent(peek().getLocalToParent().times(T));
    return this;
  }

  public Camera pop() {
    stack.remove(stack.size() - 1);
    return this;
  }

  public Camera translate(M3d v) {
    peek().translate(v);
    return this;
  }

  public Camera rotate(M3d v, double t) {
    peek().rotate(v, t);
    return this;
  }

  public Camera setIdentity() {
    peek().setIdentity();
    return this;
  }

  public void lookAt(M3d from, M3d dir, M3d up) {
    if (Math.abs(up.dot(dir)) > 0.9999) {
      up = up
          .plus((Math.abs(up.getZ()) > 0.999) ? new M3d(0, 0.1, 0) : new M3d(0, 0, -0.1))
          .normalized();
    }

    M3d zaxis = dir.normalized().times(-1);
    M3d xaxis = up.cross(zaxis).normalized();
    M3d yaxis = zaxis.cross(xaxis);

    setLocalToParent(new M4x4(
        xaxis.getX(),      yaxis.getX(),      zaxis.getX(),     0,
        xaxis.getY(),      yaxis.getY(),      zaxis.getY(),     0,
        xaxis.getZ(),      yaxis.getZ(),      zaxis.getZ(),     0,
        0, 0, 0, 1).transposed());
    translate(from);
  }

  public M3d getPosition() {
    return getLocalToParent().times(new M3d(0,0,0));
  }

  public M3d getDirection() {
    return getLocalToParent().extract3x3().times(new M3d(0, 0, -1)).normalized();
  }

  public M3d getUp() {
    return getLocalToParent().extract3x3().times(new M3d(0, 1, 0)).normalized();
  }

  public M3d getRight() {
    return getLocalToParent().extract3x3().times(new M3d(1, 0, 0)).normalized();
  }

  public double getDistanceToViewingPlane() {
    return 0.414;
  }

  public double getViewWidth() {
    return Math.sin(FOV * Math.PI / 360.0) * 2.0;
  }

  public double getViewHeight() {
    return Math.sin(FOV * Math.PI / 360.0) * 2.0;
  }

  public void straighten() {
    M3d up = getUp();
    M3d dir = getDirection();
    M3d planeNormal = dir.cross(new M3d(0, 1, 0));
    M3d targetUp = planeNormal.cross(dir).normalized();
    M3d center = getPosition();
    double sign = (targetUp.cross(up).dot(dir) >= 0) ? 1 : -1;
    double theta = sign * Math.acos(targetUp.dot(up));
    translate(center.times(-1));
    rotate(dir, theta);
    translate(center);
  }

  private Transformable peek() {
    return stack.get(stack.size() - 1);
  }
}
