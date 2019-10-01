package com.bentonian.framework.mesh.implicits;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.implicits.Octree.State;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class ImplicitSurfaceRefiner {

  private static final int DEFAULT_TARGET_LEVEL = 3;
  private static final long REFINE_TIME_PER_FRAME_MILLIS = 250;

  protected /* final */ ForceFunction forceFunction;
  protected final LinkedList<Octree> inProgress;
  protected final List<Octree> roots;
  
  protected final Vec3 min;
  protected final double scale;
  protected final int fx, fy, fz;

  private int targetLevel;
  private final List<Octree> almostFinished;
  private final List<Octree> finished;


  ////////////////////////////////////////

  public ImplicitSurfaceRefiner(Vec3 min, Vec3 max, ForceFunction forceFunction) {
    double dx = max.getX() - min.getX();
    double dy = max.getY() - min.getY();
    double dz = max.getZ() - min.getZ();

    this.min = min;
    this.targetLevel = DEFAULT_TARGET_LEVEL;
    this.inProgress = Lists.newLinkedList();
    this.almostFinished = Lists.newArrayList();
    this.finished = Lists.newLinkedList();
    this.roots = Lists.newLinkedList();
    this.forceFunction = forceFunction;
    
    this.scale = (dx < dy && dx < dz) ? dx : (dy < dx && dy < dz) ? dy : dz;
    this.fx = (int) Math.ceil(dx / scale);
    this.fy = (int) Math.ceil(dy / scale);
    this.fz = (int) Math.ceil(dz / scale);
  }
  
  public ForceFunction getForceFunction() {
    return forceFunction;
  }

  public ImplicitSurfaceRefiner reset() {
    forceFunction.reset();
    inProgress.clear();
    almostFinished.clear();
    finished.clear();
    roots.clear();
    findRootOctrees();
    return this;
  }
  
  protected void findRootOctrees() {
    Sample[][][] initialSamples = sampleGrid(min, scale, fx+1, fy+1, fz+1, Optional.empty());
    Set<Vec3> targets = forceFunction.getTargets();

    for (int x = 0; x < fx; x++) {
      for (int y = 0; y < fy; y++) {
        for (int z = 0; z < fz; z++) {
          scheduleRefinement(new Octree(forceFunction, null, 0, initialSamples, x, y, z, targets));
        }
      }
    }
    roots.addAll(inProgress);
  }

  public List<Octree> getRoots() {
    return roots;
  }
  
  public Iterable<Octree> getKnownOctrees() {
    return Iterables.concat(inProgress, finished);
  }

  public int getTargetLevel() {
    return targetLevel;
  }

  public boolean setTargetLevel(int targetLevel) {
    if (this.targetLevel != targetLevel) {
      this.targetLevel = targetLevel;
      resetInProgressAndFinished();
      return true;
    } else {
      return true;
    }
  }

  /**
   * Runs the refine algorithm until all interesting octrees are at the target level.
   */
  public ImplicitSurfaceRefiner refineCompletely() {
    while (!inProgress.isEmpty() || !almostFinished.isEmpty()) {
      refine();
    }
    return this;
  }
  
  public boolean isRefined() {
    return !roots.isEmpty() && inProgress.isEmpty();
  }

  /**
   * Runs the refine algorithm for {@link REFINE_TIME_PER_FRAME_MILLIS} millis.
   * 
   * Returns true if the final octree set was changed.
   */
  public boolean refine() {
    long timeout = System.currentTimeMillis() + REFINE_TIME_PER_FRAME_MILLIS;
    boolean unfinished = roots.isEmpty() || !inProgress.isEmpty();

    if (unfinished) {
      refineInProgress(timeout);
    }
    
    unfinished |= !almostFinished.isEmpty();
    
    if (inProgress.isEmpty() && !almostFinished.isEmpty()) {
      checkForMissedFaces();
    }

    return unfinished;
  }
  
  protected void refineInProgress(long timeout) {
    while (!inProgress.isEmpty() && System.currentTimeMillis() < timeout) {
      refine(inProgress.remove());
    }
  }

  protected void refine(Octree octree) {
    if (octree.getChildOctrees() == null) {
      double scale = (octree.getMax().getX() - octree.getMin().getX()) / 2.0;
      Sample[][][] childSamples = sampleGrid(octree.getMin(), scale, 3, 3, 3, Optional.of(octree));
      octree.addChildren(childSamples);
    }
    
    octree.setState(State.REFINED);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        for (int k = 0; k < 2; k++) {
          Octree child = octree.getChildOctrees()[i][j][k];
          if (child.getLevel() < targetLevel && child.isInteresting()) {
            scheduleRefinement(child);
          } else if (child.hasPolygons()) {
            finished.add(child);
            almostFinished.add(child);
            child.setState(State.SCHEDULED_FOR_MISSED_FACES_CHECK);
          }
        }
      }
    }
  }

  protected Sample[][][] sampleGrid(Vec3 min, double scale, int numX, int numY, int numZ,
      Optional<Octree> precomputedCorners) {
    Sample[][][] samples = new Sample[numX][numY][numZ];
    
    for (int x = 0; x < numX; x++) {
      for (int y = 0; y < numY; y++) {
        for (int z = 0; z < numZ; z++) {
          boolean isCornerSample = (x == 0 || x == numX - 1) 
              && (y == 0 || y == numY - 1)
              && (z == 0 || z == numZ - 1);
          
          if (isCornerSample && precomputedCorners.isPresent()) {
            samples[x][y][z] = precomputedCorners.get().getCorner(
                x == 0 ? 0 : 1, y == 0 ? 0 : 1, z == 0 ? 0 : 1);
          } else {
            samples[x][y][z] = forceFunction.sample(new Vec3(
                min.getX() + x * scale,
                min.getY() + y * scale,
                min.getZ() + z * scale));
          }
        }
      }
    }

    return samples;
  }

  protected void scheduleRefinement(Octree octree) {
    inProgress.addLast(octree);
    octree.setState(State.SCHEDULED_FOR_REFINEMENT);
  }

  protected void checkForMissedFaces() {
    for (Octree octree : almostFinished) {
      octree.setState(State.FINISHED);
      for (int face = 0; face < 6; face++) {
        if (octree.hasInterestingFace(face)) {
          Vec3 pt = octree.getPointJustBeyondFace(face);
          Octree op = findContainer(octree, pt);

          if (op != null 
              && op.getLevel() < octree.getLevel() 
              && !op.getState().equals(State.SCHEDULED_FOR_REFINEMENT)) {
            op.addTarget(pt);
            scheduleRefinement(op);
          }
        }
      }
    }
    almostFinished.clear();
  }

  private Octree findContainer(Octree cousin, Vec3 pt) {
    Octree ancestor = cousin.getParent();
    while (ancestor != null) {
      if (ancestor.encloses(pt)) {
        return findContainerInChildren(pt, ancestor);
      } else {
        ancestor = ancestor.getParent();
      }
    }
    return findContainerFromAllRoots(pt);
  }

  private Octree findContainerFromAllRoots(Vec3 pt) {
    for (Octree root : roots) {
      if (root.encloses(pt)) {
        Octree container = findContainerInChildren(pt, root);
        if (container != null) {
          return container;
        }
      }
    }
    return null;
  }

  private Octree findContainerInChildren(Vec3 pt, Octree parent) {
    Octree[][][] kids = parent.getChildOctrees();

    if (parent.encloses(pt)) {
      if (kids != null) {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
              if (kids[i][j][k] != null) {
                Octree container = findContainerInChildren(pt, kids[i][j][k]);
                if (container != null) {
                  return container;
                }
              }
            }
          }
        }
      }

      return parent;
    }

    return null;
  }

  private void resetInProgressAndFinished() {
    inProgress.clear();
    almostFinished.clear();
    finished.clear();
    for (Octree octree : roots) {
      udpateInProgressAndFinished(octree);
    }
  }

  private void udpateInProgressAndFinished(Octree octree) {
    if (octree.getLevel() < targetLevel) {
      if (octree.getChildOctrees() == null) {
        scheduleRefinement(octree);
      } else {
        for (int i = 0; i < 2; i++) {
          for (int j = 0; j < 2; j++) {
            for (int k = 0; k < 2; k++) {
              Octree child = octree.getChildOctrees()[i][j][k];
              if (child != null) {
                udpateInProgressAndFinished(child);
              }
            }
          }
        }
      }
    } else if (octree.getLevel() == targetLevel && octree.hasPolygons()) {
      finished.add(octree);
      almostFinished.add(octree);
      octree.setState(State.SCHEDULED_FOR_MISSED_FACES_CHECK);
    }
  }
}
