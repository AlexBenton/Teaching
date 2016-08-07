package com.bentonian.gldemos.subdivision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.bentonian.framework.animation.AnimatedMeshPrimitive;
import com.bentonian.framework.animation.AnimatingSubdivisionFunctionFactory;
import com.bentonian.framework.io.OFFUtil;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshUtil;
import com.bentonian.framework.mesh.primitive.MeshPrimitive;
import com.bentonian.framework.mesh.primitive.MeshPrimitiveFeatureAccelerator;
import com.bentonian.framework.mesh.subdivision.SubdivisionFunction;
import com.bentonian.framework.ui.DemoApp;

public class SubdivisionDemo extends DemoApp {
  
  private static final M3d BLUE = new M3d(0, 0, 1);
  private static final int NUM_FUNCTIONS = 3;

  private static final String[] FN_NAMES = { "Loop", "Doo-Sabin", "Catmull-Clark" };
  private static final String[] GEOMETRY_NAMES = { "Cube", "Tetrahedron", "Dodecahedron", "Box Cube", "Plus", "Cow" };
  private static final Mesh[] GEOMETRY = { 
    OFFUtil.parseFile("off/cube.off").centerAtOrigin(),
    OFFUtil.parseFile("off/tetrahedron.off").centerAtOrigin(),
    OFFUtil.parseFile("off/dodecahedron.off").centerAtOrigin(),
    OFFUtil.parseFile("off/boxcube.off").centerAtOrigin(),
    plus(),
    OFFUtil.parseFile("cow.off").centerAtOrigin(), 
  };
  private static final MeshPrimitiveFeatureAccelerator[] ORIGINAL_GEOMETRY_EDGES = buildOriginalEdges();

  private final Map<Coord, List<MeshPrimitive>> levels = new HashMap<>();
  private final Map<Coord, List<AnimatedMeshPrimitive>> animations = new HashMap<>();

  private Coord coord = new Coord(0, 0);
  private int index = 0;
  private boolean showEdges = true;
  private boolean showNormals = false;
  private AnimatedMeshPrimitive currentAnimation = null;

  protected SubdivisionDemo() {
    super("Subdivision Demo");
    setCameraDistance(4);
    for (int fn = 0; fn < NUM_FUNCTIONS; fn++) {
      for (int geom = 0; geom < GEOMETRY.length; geom++) {
        Coord coord = new Coord(fn, geom);
        animations.put(coord, new ArrayList<AnimatedMeshPrimitive>());
        levels.put(coord, new ArrayList<MeshPrimitive>());
        levels.get(coord).add(new MeshPrimitive(GEOMETRY[geom]));
        levels.get(coord).get(0).getFeaturesAccelerator().setShowEdges(showEdges);
        levels.get(coord).get(0).getFeaturesAccelerator().setShowNormals(showNormals);
      }
    }
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case GLFW.GLFW_KEY_MINUS:
      if (index > 0) {
        index--;
        if (!GEOMETRY_NAMES[coord.geom].equals("Cow")) {
          currentAnimation = animations.get(coord).get(index);
          currentAnimation.animate(5000, 1, 0);
        }
      }
      break;
    case GLFW.GLFW_KEY_EQUAL:
      if (index == levels.get(coord).size() - 1) {
        addLevel();
      }
      if (!GEOMETRY_NAMES[coord.geom].equals("Cow")) {
        currentAnimation = animations.get(coord).get(index);
        currentAnimation.animate(5000, 0, 1);
      }
      index++;
      break;
    case GLFW.GLFW_KEY_LEFT_BRACKET:
      coord.fn = (coord.fn + NUM_FUNCTIONS - 1) % NUM_FUNCTIONS;
      currentAnimation = null;
      capIndex();
      break;
    case GLFW.GLFW_KEY_RIGHT_BRACKET:
      coord.fn = (coord.fn + 1) % NUM_FUNCTIONS;
      currentAnimation = null;
      capIndex();
      break;
    case GLFW.GLFW_KEY_K:
      coord.geom = (coord.geom + GEOMETRY.length - 1) % GEOMETRY.length;
      currentAnimation = null;
      capIndex();
      break;
    case GLFW.GLFW_KEY_J:
      coord.geom = (coord.geom + 1) % GEOMETRY.length;
      currentAnimation = null;
      capIndex();
      break;
    case GLFW.GLFW_KEY_E:
      showEdges = !showEdges;
      updateFeatures();
      break;
    case GLFW.GLFW_KEY_N:
      showNormals = !showNormals;
      updateFeatures();
      break;
    default:
      super.onKeyDown(key);
      break;
    }
    updateTitle();
  }

  @Override
  public void draw() {
    if (currentAnimation != null && currentAnimation.isAnimating()) {
      currentAnimation.render(this);
    } else {
      levels.get(coord).get(index).render(this);
    }
    ORIGINAL_GEOMETRY_EDGES[coord.geom].render(this);
  }

  private void addLevel() {
    AnimatedMeshPrimitive animation = new AnimatedMeshPrimitive();
    SubdivisionFunction fn = null;

    switch (FN_NAMES[coord.fn]) {
    case "Loop" : fn = AnimatingSubdivisionFunctionFactory.buildLoop(animation); break;
    case "Doo-Sabin" : fn = AnimatingSubdivisionFunctionFactory.buildDooSabin(animation); break;
    case "Catmull-Clark" : fn = AnimatingSubdivisionFunctionFactory.buildCatmullClark(animation); break;
    }
    
    Mesh nextLevel = fn.apply(levels.get(coord).get(index).getMesh());
    MeshPrimitive nextLevelPrimitive = new MeshPrimitive(nextLevel);
    nextLevelPrimitive.getFeaturesAccelerator().setShowEdges(showEdges);
    nextLevelPrimitive.getFeaturesAccelerator().setShowNormals(showNormals);
    levels.get(coord).add(nextLevelPrimitive);
    animations.get(coord).add(animation);
    animation.getFeaturesAccelerator().setShowEdges(showEdges, BLUE);
    animation.getFeaturesAccelerator().setShowNormals(showNormals);
  }

  private void capIndex() {
    if (index >= levels.get(coord).size()) {
      index = levels.get(coord).size() - 1;
    }
  }

  private void updateTitle() {
    setTitle("Subdivision Demo - " + GEOMETRY_NAMES[coord.geom] 
        + " - " + FN_NAMES[coord.fn] + ", level " + index
        + " (" + levels.get(coord).get(index).getMesh().size() + " faces)");
  }
  
  private void updateFeatures() {
    for (int fn = 0; fn < NUM_FUNCTIONS; fn++) {
      for (int geom = 0; geom < GEOMETRY.length; geom++) {
        Coord coord = new Coord(fn, geom);
        for (MeshPrimitive primitive : levels.get(coord)) {
          primitive.getFeaturesAccelerator().setShowEdges(showEdges, BLUE);
          primitive.getFeaturesAccelerator().setShowNormals(showNormals);
        }
        for (MeshPrimitive animation : animations.get(coord)) {
          animation.getFeaturesAccelerator().setShowEdges(showEdges, BLUE);
          animation.getFeaturesAccelerator().setShowNormals(showNormals);
        }
      }
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private static class Coord {
    int fn;
    int geom;

    Coord(int fn, int geom) {
      this.fn = fn;
      this.geom = geom;
    }

    @Override
    public int hashCode() {
      return fn * 10000 + geom;
    }

    @Override
    public boolean equals(Object obj) {
      return (obj instanceof Coord) && ((Coord) obj).fn == fn && ((Coord) obj).geom == geom;
    }
  }
  
  private static Mesh plus() {
    Mesh cube = OFFUtil.parseFile("off/cube.off");
    Mesh plus = new Mesh();

    plus.copy(cube);
    for (M3d offset : new M3d[] { new M3d(1, 0, 0), new M3d(0, 1, 0), new M3d(0, 0, 1) }) {
      for (int i = -1; i <= 1; i += 2) {
        plus = MeshUtil.union(plus, MeshUtil.translate(cube, offset.times(2 * i)));
        plus = MeshUtil.difference(plus, MeshUtil.translate(cube, offset.times(4 * i)));
      }
    }
    return plus;
  }

  private static MeshPrimitiveFeatureAccelerator[] buildOriginalEdges() {
    MeshPrimitiveFeatureAccelerator[] originals = 
        new MeshPrimitiveFeatureAccelerator[GEOMETRY.length];
    for (int i = 0; i < GEOMETRY.length; i++) {
      originals[i] = new MeshPrimitiveFeatureAccelerator(
          GEOMETRY_NAMES[i].equals("Cow") ? new Mesh() : GEOMETRY[i]);
    }
    return originals;
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new SubdivisionDemo().run();
  }
}
