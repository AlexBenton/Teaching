package com.bentonian.framework.scene;

import java.util.ArrayList;
import java.util.List;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.math.M4x4;

/**
 * Pairing of M4x4s representing the cameraToWorld and worldToCamera transforms.
 */
public class Camera {

  private static final double FOV = 46;

  private final List<Transformable> stack;

  public Camera() {
    this.stack = new ArrayList<>();
    stack.add(new Transformable());
    translate(new Vec3(0, 0, 1));
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

  public Camera translate(Vec3 v) {
    peek().translate(v);
    return this;
  }

  public Camera rotate(Vec3 v, double t) {
    peek().rotate(v, t);
    return this;
  }

  public Camera setIdentity() {
    peek().setIdentity();
    return this;
  }

  public void lookAt(Vec3 from, Vec3 dir, Vec3 up) {
    if (Math.abs(up.dot(dir)) > 0.9999) {
      up = up
          .plus((Math.abs(up.getZ()) > 0.999) ? new Vec3(0, 0.1, 0) : new Vec3(0, 0, -0.1))
          .normalized();
    }

    Vec3 zaxis = dir.normalized().times(-1);
    Vec3 xaxis = up.cross(zaxis).normalized();
    Vec3 yaxis = zaxis.cross(xaxis);

    setLocalToParent(new M4x4(
        xaxis.getX(),      yaxis.getX(),      zaxis.getX(),     0,
        xaxis.getY(),      yaxis.getY(),      zaxis.getY(),     0,
        xaxis.getZ(),      yaxis.getZ(),      zaxis.getZ(),     0,
        0, 0, 0, 1).transposed());
    translate(from);
  }

  public Vec3 getPosition() {
    return getLocalToParent().times(new Vec3(0,0,0));
  }

  public Vec3 getDirection() {
    return getLocalToParent().extract3x3().times(new Vec3(0, 0, -1)).normalized();
  }

  public Vec3 getUp() {
    return getLocalToParent().extract3x3().times(new Vec3(0, 1, 0)).normalized();
  }

  public Vec3 getRight() {
    return getLocalToParent().extract3x3().times(new Vec3(1, 0, 0)).normalized();
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

  private Transformable peek() {
    return stack.get(stack.size() - 1);
  }
}
