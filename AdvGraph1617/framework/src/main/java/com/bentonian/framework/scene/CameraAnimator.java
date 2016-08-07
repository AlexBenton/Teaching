package com.bentonian.framework.scene;

import com.bentonian.framework.math.M3d;

public class CameraAnimator {
  
  private final Camera camera;
  private final Keyframe from;
  private final Keyframe to;
  
  public CameraAnimator(Camera camera, M3d lookFrom, M3d lookDir, M3d lookUp, long millis) {
    this.camera = camera;
    this.from = new Keyframe(camera.getPosition(), camera.getDirection(), camera.getUp(), System.currentTimeMillis());
    this.to = new Keyframe(lookFrom, lookDir, lookUp, System.currentTimeMillis() + millis);
  }
  
  public void apply() {
    long now = System.currentTimeMillis();
    double t = Math.min(1, (now - from.millis) / (double) (to.millis - from.millis));
  
    // TODO Use quaternions for a great-circle interpolation instead of linear interpolation.
    M3d pt = from.pos.plus(to.pos.minus(from.pos).times(t));
    M3d dir = from.dir.plus(to.dir.minus(from.dir).times(t)).normalized();
    M3d up = from.up.plus(to.up.minus(from.up).times(t)).normalized();
    camera.lookAt(pt, dir, up);
  }
  
  public boolean isDone() {
    long now = System.currentTimeMillis();
    return now > to.millis;
  }
  
  private static class Keyframe {
    M3d pos;
    M3d dir;
    M3d up;
    long millis;

    public Keyframe(M3d pos, M3d dir, M3d up, long millis) {
      this.pos = pos;
      this.dir = dir.normalized();
      this.up = up.normalized();
      this.millis = millis;

      if (Math.abs(up.dot(dir)) > 0.9999) {
        up = up
            .plus((Math.abs(up.getZ()) > 0.999) ? new M3d(0, 0.1, 0) : new M3d(0, 0, -0.1))
            .normalized();
      }
    }
  }
}
