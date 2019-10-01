package com.bentonian.framework.scene;

import com.bentonian.framework.math.Vec3;

public class CameraAnimator {
  
  private final Camera camera;
  private final Keyframe from;
  private final Keyframe to;
  
  public CameraAnimator(Camera camera, Vec3 lookFrom, Vec3 lookDir, Vec3 lookUp, long millis) {
    this.camera = camera;
    this.from = new Keyframe(camera.getPosition(), camera.getDirection(), camera.getUp(), System.currentTimeMillis());
    this.to = new Keyframe(lookFrom, lookDir, lookUp, System.currentTimeMillis() + millis);
  }
  
  public void apply() {
    long now = System.currentTimeMillis();
    double t = Math.min(1, (now - from.millis) / (double) (to.millis - from.millis));
  
    // TODO Use quaternions for a great-circle interpolation instead of linear interpolation.
    Vec3 pt = from.pos.plus(to.pos.minus(from.pos).times(t));
    Vec3 dir = from.dir.plus(to.dir.minus(from.dir).times(t)).normalized();
    Vec3 up = from.up.plus(to.up.minus(from.up).times(t)).normalized();
    camera.lookAt(pt, dir, up);
  }
  
  public boolean isDone() {
    long now = System.currentTimeMillis();
    return now > to.millis;
  }
  
  private static class Keyframe {
    Vec3 pos;
    Vec3 dir;
    Vec3 up;
    long millis;

    public Keyframe(Vec3 pos, Vec3 dir, Vec3 up, long millis) {
      this.pos = pos;
      this.dir = dir.normalized();
      this.up = up.normalized();
      this.millis = millis;

      if (Math.abs(up.dot(dir)) > 0.9999) {
        up = up
            .plus((Math.abs(up.getZ()) > 0.999) ? new Vec3(0, 0.1, 0) : new Vec3(0, 0, -0.1))
            .normalized();
      }
    }
  }
}
