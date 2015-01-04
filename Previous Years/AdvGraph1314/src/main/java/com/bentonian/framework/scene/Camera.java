package com.bentonian.framework.scene;

import com.bentonian.framework.math.M3d;



/**
 * Pairing of M4x4s representing the cameraToWorld and worldToCamera transforms.
 *
 * @author Alex Benton
 */
public class Camera extends Transformable {
  
  private static final double FOV = 46;
  
  public Camera() {
  }

  public Camera(Camera source) {
    super(source);
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
}
