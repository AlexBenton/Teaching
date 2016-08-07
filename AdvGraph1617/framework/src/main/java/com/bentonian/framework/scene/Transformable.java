package com.bentonian.framework.scene;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;


/**
 * Common base class for all entities which can be transformed
 * relative to their parent context.
 */
public class Transformable {

  private final M4x4 localToParent;

  private M4x4 inverse;

  public Transformable() {
    this.localToParent = new M4x4();
    setIdentity();
  }

  public Transformable(Transformable source) {
    this.localToParent = new M4x4(source.localToParent);
    this.inverse = (source.inverse == null) ? null : new M4x4(source.inverse);
  }

  public M3d getPosition() {
    return localToParent.times(new M3d(0,0,0));
  }

  public M4x4 getLocalToParent() {
    return localToParent;
  }

  public M4x4 getParentToLocal() {
    if (inverse == null) {
      inverse = localToParent.inverted();
    }
    return inverse;
  }
  
  public Transformable setIdentity() {
    localToParent.setIdentity();
    forget();
    return this;
  }

  public Transformable setLocalToParent(M4x4 l2p) {
    localToParent.setData(l2p);
    forget();
    return this;
  }

  public Transformable apply(M4x4 T) {
    return setLocalToParent(T.times(localToParent));
  }

  public Transformable translate(M3d v) {
    return apply(M4x4.translationMatrix(v));
  }

  public Transformable rotate(M3d axis, double d) {
    return apply(M4x4.rotationMatrix(axis, d));
  }

  public Transformable scale(M3d v) {
    return apply(M4x4.scaleMatrix(v));
  }
  
  public Transformable scale(double d) {
    return apply(M4x4.scaleMatrix(d));
  }
  private void forget() {
    inverse = null;
  }
}
