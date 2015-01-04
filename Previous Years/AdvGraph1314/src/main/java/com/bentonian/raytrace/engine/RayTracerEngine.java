package com.bentonian.raytrace.engine;

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.Map;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.math.MatrixStack;
import com.bentonian.framework.math.Ray;
import com.bentonian.framework.math.RayIntersection;
import com.bentonian.framework.math.RayIntersections;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.scene.IsRayTraceable;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.scene.PrimitiveCollection;
import com.bentonian.framework.ui.RGBCanvas;
import com.google.common.collect.Maps;

public class RayTracerEngine {

  public static final double MIN_TRAVEL = 0.001;
  private static final int NUM_LEVELS = 3;

  private static final Map<M4x4, LocalToWorldVariations> LOCAL_TO_WORLD_VARIATIONS =
      Maps.newHashMap();

  private final Scene scene;
  private final Camera camera;

  private M3d up, right, dir, pos;
  private double width, height, distToPlane;
  private int canvasWidth, canvasHeight;
  private M3d background = new M3d(1, 1, 1);
  private int numShadowRays;
  private double lightRadius;

  private RGBCanvas canvas;
  int supersamplingMultiple;
  int renderProgress;

  public RayTracerEngine(Scene scene, RGBCanvas canvas, Camera camera) {
    this.scene = scene;
    this.canvas = canvas;
    this.camera = camera;
    this.supersamplingMultiple = 1;
    this.renderProgress = 0;
    this.numShadowRays = 1;
    this.lightRadius = 0;
  }

  public void renderToCanvas() {
    long then = System.currentTimeMillis();

    canvasWidth = canvas.getWidth();
    canvasHeight = canvas.getHeight();
    up = camera.getUp();
    right = camera.getRight();
    dir = camera.getDirection();
    pos = camera.getPosition();
    width = camera.getViewWidth();
    height = camera.getViewHeight();
    distToPlane = camera.getDistanceToViewingPlane();

    System.out.println("Rendering (" + canvasWidth + " x " + canvasHeight + ", "
        + supersamplingMultiple + "x supersample)...");
    int max = ((canvasWidth - 1) * (canvasHeight - 1));
    for (int x = 0; x < canvasWidth; x++) {
      for (int y = 0; y < canvasHeight; y++) {
        renderProgress = ((x * (canvasHeight - 1)) + y) * 100 / max;
        fireRay(x, y);
      }
    }
    long now = System.currentTimeMillis();
    System.out.println("...render complete (" + ((now - then) / 1000) + "s)");
  }

  public int getRenderProgress() {
    return renderProgress;
  }

  public void setCanvas(RGBCanvas canvas) {
    this.canvas = canvas;
  }

  public void setSupersamplingMultiple(int multiple) {
    this.supersamplingMultiple = multiple;
  }
  
  public int getSupersamplingMultiple() {
    return supersamplingMultiple;
  }
  
  public void setNumShadowRays(int numShadowRays) {
    this.numShadowRays = numShadowRays;
  }
  
  public void setLightRadius(double lightRadius) {
    this.lightRadius = lightRadius;
  }

  public void setBackground(M3d background) {
    this.background = background;
  }

  public static Ray getCameraRay(Camera camera, int x, int y, double w, double h) {
    double aspectRatio = w / h;
    double cellLeft = (camera.getViewWidth()/2) * ((x - (w/2)) / w) * aspectRatio;
    double cellRight = (camera.getViewWidth()/2) * (((x+1) - (w/2)) / w) * aspectRatio;
    double cellTop = (camera.getViewHeight()/2) * ((y - (h/2)) / h);
    double cellBottom = (camera.getViewHeight()/2) * (((y+1) - (h/2)) / h);
    return computeRay(
        camera.getPosition(), camera.getDirection(), camera.getRight(), camera.getUp(),
        camera.getDistanceToViewingPlane(), cellLeft, cellRight, cellTop, cellBottom);
  }

  public static RayIntersections traceScene(Primitive primitive, Ray ray) {
    RayIntersections hits = new RayIntersections();
    tracePrimitive(primitive, new MatrixStack(), hits, ray);
    return hits;
  }

  /////////////////////////////////////////////////////////////////////////////

  private static Ray computeRay(M3d pos, M3d dir, M3d right, M3d up,
      double distToPlane, double cellLeft, double cellRight, double cellTop, double cellBottom) {
    double cellX = (cellLeft + cellRight) / 2;
    double cellY = (cellTop + cellBottom) / 2;
    M3d interceptRight = right.times(cellX);
    M3d interceptUp = up.times(cellY);
    M3d intercept = pos
        .plus(dir.times(distToPlane))
        .plus(interceptUp)
        .plus(interceptRight);
    return new Ray(pos, intercept.minus(pos).normalized());
  }

  private void fireRay(int x, int y) {
    M3d color = null;

    for (int i = 0; i < supersamplingMultiple; i++) {
      for (int j = 0; j < supersamplingMultiple; j++) {
        M3d C = fireRay(
            x * supersamplingMultiple + i,
            y * supersamplingMultiple + j,
            canvasWidth * supersamplingMultiple,
            canvasHeight * supersamplingMultiple);
        color = (color == null) ? C : color.plus(C);
      }
    }
    color = color.times(1.0 / (supersamplingMultiple * supersamplingMultiple));
    canvas.putPixel(x, (canvasHeight-1) - y, color);
  }

  private M3d fireRay(int x, int y, double w, double h) {
    Ray ray = getCameraRay(x, y, w, h);
    RayIntersections hits = traceScene(scene, ray);
    return hits.isEmpty() ? background : illuminate(scene, ray, hits.getNearest(), 0);
  }

  private Ray getCameraRay(int x, int y, double w, double h) {
    double aspectRatio = w / h;
    double cellLeft = (width/2) * ((x - (w/2)) / w) * aspectRatio;
    double cellRight = (width/2) * (((x+1) - (w/2)) / w) * aspectRatio;
    double cellTop = (height/2) * ((y - (h/2)) / h);
    double cellBottom = (height/2) * (((y+1) - (h/2)) / h);
    return computeRay(pos, dir, right, up, distToPlane, cellLeft, cellRight, cellTop, cellBottom);
  }

  private M3d illuminate(Scene scene, Ray eyeRay, RayIntersection hit, int numLevels) {
    final Material hitMaterial = hit.material;
    double diffuseSum = 0;
    double specularSum = 0;
    M3d N = hit.normal;
    M3d E = eyeRay.direction.times(-1);
    M3d nVDotN = N.times(N.dot(eyeRay.direction));
    M3d reflectedColor = new M3d(0,0,0);
    M3d transparencyColor = new M3d(0,0,0);
    M3d baseColor = hitMaterial.getColor();

    for (M3d light : scene.getLights()) {
      M3d L = light.minus(hit.point).normalized();

      if (N.dot(L) > 0) {
        // Shadow test
        double shaded = (numShadowRays != 0) ? getShadow(scene, hit.point, light) : 1;
  
        if (shaded > 0) {
          // Diffuse
          diffuseSum += shaded * N.dot(L);
  
          // Specular
          M3d R = N.times(2*(L.dot(N))).minus(L);
          specularSum += shaded * pow(max(R.dot(E), 0), hitMaterial.getSpecularShininess());
        }
      }
    }

    // Reflection
    if ((numLevels < NUM_LEVELS) && (hitMaterial.getReflectivity() > 0)) {
      M3d reflection = eyeRay.direction.minus(nVDotN.times(2)).normalized();
      Ray eyeRayReflected = new Ray(hit.point, reflection);

      reflectedColor = secondaryRay(scene, eyeRayReflected, numLevels);
      reflectedColor = reflectedColor.times(hitMaterial.getReflectivity());
    }

    // Transparency and refraction
    if ((numLevels < NUM_LEVELS) && (hitMaterial.getTransparency() > 0)) {
      Ray eyeRayPassedThrough;

      if ((hitMaterial.getRefractiveIndex() == 1.0) || (Math.abs(E.dot(N)) > 0.99999)) {
        eyeRayPassedThrough = new Ray(hit.point, eyeRay.direction);
      } else {
        boolean nPointsTowardsRayOrigin = N.dot(E) >= 0;
        M3d nOne = nPointsTowardsRayOrigin ? N : N.times(-1);
        M3d nTwo = nPointsTowardsRayOrigin ? N.times(-1) : N;
        double rOne = nPointsTowardsRayOrigin ? 1.0 : hitMaterial.getRefractiveIndex();
        double rTwo = nPointsTowardsRayOrigin ? hitMaterial.getRefractiveIndex() : 1.0;
        double thetaOne = acos(Math.abs(nOne.dot(E)));
        double thetaTwo = asin(sin(thetaOne) * rOne / rTwo);
        M3d axis = eyeRay.direction.cross(nTwo);
        M4x4 bend = M4x4.rotationMatrix(axis, thetaTwo);

        eyeRayPassedThrough = new Ray(hit.point, bend.times(nTwo).normalized());
      }
      transparencyColor = secondaryRay(scene, eyeRayPassedThrough, numLevels);
      transparencyColor = transparencyColor.times(hitMaterial.getTransparency());
    }

    double localLighting =
        hitMaterial.getKa() +
        diffuseSum * hitMaterial.getKd() +
        specularSum * hitMaterial.getKs();
    M3d localColor = baseColor.times(localLighting).times(
        1 - Math.max(hitMaterial.getReflectivity(), hitMaterial.getTransparency()));
    M3d totalColor = localColor.plus(reflectedColor).plus(transparencyColor);
    return totalColor;
  }
  
  private double getShadow(Scene scene, M3d P, M3d L) {
    M3d dir = L.normalized();
    int numRaysThatReachedTheLight = 0;
    double maxT = L.minus(P).length();
    
    for (int i = 0; i < numShadowRays; i++) {
      RayIntersections hits = traceScene(scene, new Ray(P, dir));
      if (hits.isEmpty() || hits.getNearest().t > maxT) {
        numRaysThatReachedTheLight++;
      }
      dir = L.plus(new M3d(
          1 - 2 * Math.random(), 
          1 - 2 * Math.random(), 
          1 - 2 * Math.random()).times(lightRadius)).normalized();
    }
    return ((double) numRaysThatReachedTheLight) / ((double) numShadowRays);
  }

  private M3d secondaryRay(Scene scene, Ray ray, int numLevels) {
    RayIntersections hits = traceScene(scene, ray);
    return hits.isEmpty() ? background : illuminate(scene, ray, hits.getNearest(), numLevels+1);
  }

  private static void tracePrimitive(Primitive primitive, MatrixStack localToWorldStack,
      RayIntersections hits, Ray ray) {
    localToWorldStack.push(primitive.getLocalToParent());

    if (primitive instanceof IsRayTraceable) {
      M4x4 localToWorld = localToWorldStack.peek();
      LocalToWorldVariations variationsOnATheme = getVariations(localToWorld);
      RayIntersections traced = ((IsRayTraceable) primitive).traceLocal(ray.transformedBy(
          variationsOnATheme.getWorldToLocal(), variationsOnATheme.getWorldToLocal3x3()));

      if ((traced != null) && !traced.isEmpty()) {
        for (RayIntersection collision : traced) {
          M3d normal = variationsOnATheme
              .getLocalNormalToWorldNormal()
              .times(collision.normal)
              .normalized();
          M3d point = localToWorld
              .times(collision.point);
          double t = point.minus(ray.origin).length();
          hits.add(t, point, normal, collision.material);
        }
      }
    }

    if (primitive instanceof PrimitiveCollection) {
      for (Primitive child : ((PrimitiveCollection) primitive).getPrimitives()) {
        tracePrimitive(child, localToWorldStack, hits, ray);
      }
    }

    localToWorldStack.pop();
  }

  private static LocalToWorldVariations getVariations(M4x4 localToWorld) {
    LocalToWorldVariations variations = LOCAL_TO_WORLD_VARIATIONS.get(localToWorld);
    if (variations == null) {
      variations = new LocalToWorldVariations(localToWorld);
      LOCAL_TO_WORLD_VARIATIONS.put(localToWorld, variations);
    }
    return variations;
  }

  /////////////////////////////////////////////////////////////////////////////

  private static class LocalToWorldVariations {
    private final M4x4 localToWorld;
    private M4x4 worldToLocal;
    private M4x4 worldToLocal3x3;
    private M4x4 localNormalToWorldNormal;

    LocalToWorldVariations(M4x4 localToWorld) {
      this.localToWorld = localToWorld;
    }
    public M4x4 getWorldToLocal() {
      if (worldToLocal == null) {
        worldToLocal = localToWorld.inverted();
      }
      return worldToLocal;
    }
    public M4x4 getWorldToLocal3x3() {
      if (worldToLocal3x3 == null) {
        worldToLocal3x3 = getWorldToLocal().extract3x3();
      }
      return worldToLocal3x3;
    }
    public M4x4 getLocalNormalToWorldNormal() {
      if (localNormalToWorldNormal == null) {
        localNormalToWorldNormal = localToWorld.extract3x3().inverted().transposed();
      }
      return localNormalToWorldNormal;
    }
  }
}
