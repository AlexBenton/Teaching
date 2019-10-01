package com.bentonian.framework.mesh.implicits;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import com.bentonian.framework.material.Colors;
import com.bentonian.framework.material.HasColor;
import com.bentonian.framework.math.Vec3;
import com.google.common.collect.Sets;

public class ForceFunction {

  private final WeakHashMap<Vec3, Sample> cache;
  private boolean cachingEnabled = true;

  private List<Force> forces = new LinkedList<Force>();

  public ForceFunction() {
    this.cache = new WeakHashMap<Vec3, Sample>();
  }
  
  public double getCutoff() {
    return 0.5;
  }
  
  public boolean isHot(Sample sample) {
    return sample.getForce() > getCutoff();
  }

  public void reset() {
    cache.clear();
  }

  public ForceFunction addForce(Force f) {
    forces.add(f);
    return this;
  }

  public Set<Vec3> getTargets() {
    Set<Vec3> targets = Sets.newHashSet();

    for (Force f : forces) {
      if (f instanceof Vec3) {
        targets.add((Vec3) f);
      }
    }
    return targets;
  }

  public void setCaching(boolean cachingEnabled) {
    this.cachingEnabled = cachingEnabled;
  }
  
  public void addToCache(Vec3 v, Sample sample) {
    cache.put(v,  sample);
  }
  
  public Sample getCached(Vec3 v) {
    return cache.get(v);
  }

  public Sample sample(Vec3 v) {
    double sum = 0;
    double summedWeight = 0;
    Vec3 summedColor = new Vec3();
    Vec3 color;
    Sample sample = getCached(v);

    if (sample == null) {
      for (Force f : forces) {
        double force = f.F(v);
        sum += force;
        if (f instanceof HasColor) {
          double weight = Math.abs(force - getCutoff());
          summedWeight += weight;
          summedColor = summedColor.plus(((HasColor) f).getColor().times(weight));
        }
      }
      color = (summedWeight > 0) ? summedColor.times(1 / summedWeight) : Colors.WHITE;
      sample = new Sample(v, sum, color);
      if (cachingEnabled) {
        addToCache(v, sample);
      }
    }

    return sample;
  }
}
