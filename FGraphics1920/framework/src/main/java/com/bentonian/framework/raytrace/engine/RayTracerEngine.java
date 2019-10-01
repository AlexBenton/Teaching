package com.bentonian.framework.raytrace.engine;

import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sin;

import java.util.Map;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.Vec3;
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

  private Vec3 up, right, dir, pos;
  private double width, height, distToPlane;
  private int canvasWidth, canvasHeight;
  private Vec3 background = new Vec3(1, 1, 1);
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

  public void setBackground(Vec3 background) {
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

  private static Ray computeRay(Vec3 pos, Vec3 dir, Vec3 right, Vec3 up,
      double distToPlane, double cellLeft, double cellRight, double cellTop, double cellBottom) {
    double cellX = (cellLeft + cellRight) / 2;
    double cellY = (cellTop + cellBottom) / 2;
    Vec3 interceptRight = right.times(cellX);
    Vec3 interceptUp = up.times(cellY);
    Vec3 intercept = pos
        .plus(dir.times(distToPlane))
        .plus(interceptUp)
        .plus(interceptRight);
    return new Ray(pos, intercept.minus(pos).normalized());
  }

  private void fireRay(int x, int y) {
    Vec3 color = null;

    for (int i = 0; i < supersamplingMultiple; i++) {
      for (int j = 0; j < supersamplingMultiple; j++) {
        Vec3 C = fireRay(
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

  private Vec3 fireRay(int x, int y, double w, double h) {
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

  private Vec3 illuminate(Scene scene, Ray eyeRay, RayIntersection hit, int numLevels) {
    final Material hitMaterial = hit.material;
    double diffuseSum = 0;
    double specularSum = 0;
    Vec3 N = hit.normal;
    Vec3 E = eyeRay.direction.times(-1);
    Vec3 nVDotN = N.times(N.dot(eyeRay.direction));
    Vec3 reflectedColor = new Vec3(0,0,0);
    Vec3 transparencyColor = new Vec3(0,0,0);
    Vec3 baseColor = hitMaterial.getColor();

    for (Vec3 light : scene.getLights()) {
      Vec3 L = light.minus(hit.point).normalized();

      if (N.dot(L) > 0) {
        // Shadow test
        double shaded = (numShadowRays != 0) ? getShadow(scene, hit.point, light) : 1;
  
        if (shaded > 0) {
          // Diffuse
          diffuseSum += shaded * N.dot(L);
  
          // Specular
          Vec3 R = N.times(2*(L.dot(N))).minus(L);
          specularSum += shaded * pow(max(R.dot(E), 0), hitMaterial.getSpecularShininess());
        }
      }
    }

    // Reflection
    if ((numLevels < NUM_LEVELS) && (hitMaterial.getReflectivity() > 0)) {
      Vec3 reflection = eyeRay.direction.minus(nVDotN.times(2)).normalized();
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
        Vec3 nOne = nPointsTowardsRayOrigin ? N : N.times(-1);
        Vec3 nTwo = nPointsTowardsRayOrigin ? N.times(-1) : N;
        double rOne = nPointsTowardsRayOrigin ? 1.0 : hitMaterial.getRefractiveIndex();
        double rTwo = nPointsTowardsRayOrigin ? hitMaterial.getRefractiveIndex() : 1.0;
        double thetaOne = acos(Math.abs(nOne.dot(E)));
        double thetaTwo = asin(sin(thetaOne) * rOne / rTwo);
        Vec3 axis = eyeRay.direction.cross(nTwo);
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
    Vec3 localColor = baseColor.times(localLighting).times(
        1 - Math.max(hitMaterial.getReflectivity(), hitMaterial.getTransparency()));
    Vec3 totalColor = localColor.plus(reflectedColor).plus(transparencyColor);
    return totalColor;
  }
  
  private double getShadow(Scene scene, Vec3 P, Vec3 L) {
    Vec3 dir = L.normalized();
    int numRaysThatReachedTheLight = 0;
    double maxT = L.minus(P).length();
    
    for (int i = 0; i < numShadowRays; i++) {
      RayIntersections hits = traceScene(scene, new Ray(P, dir));
      if (hits.isEmpty() || hits.getNearest().t > maxT) {
        numRaysThatReachedTheLight++;
      }
      dir = L.plus(new Vec3(
          1 - 2 * Math.random(), 
          1 - 2 * Math.random(), 
          1 - 2 * Math.random()).times(lightRadius)).normalized();
    }
    return ((double) numRaysThatReachedTheLight) / ((double) numShadowRays);
  }

  private Vec3 secondaryRay(Scene scene, Ray ray, int numLevels) {
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
          Vec3 normal = variationsOnATheme
              .getLocalNormalToWorldNormal()
              .times(collision.normal)
              .normalized();
          Vec3 point = localToWorld
              .times(collision.point);
          double t = point.minus(ray.origin).length();
          hits.add(collision.primitive, t, point, normal, collision.material);
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
