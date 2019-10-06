// raymarching.fsh
//
// Include file for raymarching routines
// (Yes, there's no real '#include' in shaders)
//
// How to use:
// 
//   1. Declare two constants *before* including this file:
//     int renderDepth -- 400 works well
//     vec3 lightPos -- [0, 10, 10] works well
//
//   2. Declare the following two methods:
//     float f(vec3 pt);
//     SdfMaterial scene(vec3 pt);
// 
//   3. Call renderScene().  Ex:
//     fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);

#define REFRACTIVE_INDEX_OF_AIR 1.000277

////////////////////////////////////////////////////////////////////
// Data types

struct SdfMaterial {
  float sdf;
  vec3 gradient;
  Material mat;
};

////////////////////////////////////////////////////////////////////
// Forward declarations, must be implemented

// Returns the SDF (signed distance field) at pt
float f(vec3 pt);

// Returns all material traits at pt.  (Computed, probably, from f(pt).)
SdfMaterial scene(vec3 pt);

////////////////////////////////////////////////////////////////////

float weightNearZero(float f, float r) {
  return max(r - abs(fract(f + r) - r), 0.0) / r;
}

vec3 getFloorColor(vec3 pt, float d) {
  float gridWeight = max(weightNearZero(pt.x, 0.025), weightNearZero(pt.z, 0.025));
  float sdfIsocline = weightNearZero(d, 0.075);
  float distanceTaper = smoothstep(1.0, 0.0, (d - 3.0) / 3.0);
  float sdfIsoclineWeight = distanceTaper * sdfIsocline;
  
  if (sdfIsoclineWeight >= gridWeight) {
    return mix(white, blue, sdfIsoclineWeight);
  } else {
    return mix(white, black, gridWeight);
  }
}

////////////////////////////////////////////////////////////////////

bool enableShadows = true;
float shadow(vec3 pt) {
  if (!enableShadows) {
    return 1.0;
  }
  
  vec3 lightDir = normalize(lightPos - pt);
  float kd = 1;
  int step = 0;

  for (float t = 0.1; t < length(lightPos - pt) && step < renderDepth && kd > 0.001; ) {
    float d = abs(f(pt + t * lightDir));
    if (d < 0.001) {
      kd = 0;
    } else {
      kd = min(kd, 16 * d / t);
    }
    t += d;
    step++;
  }
  return kd;
}

float findInterval(vec3 rayorig, vec3 raydir, float signAtRayOrigin, float t, float d, int ticks) {
  for (int u = 0; u < ticks; u++) {
    float sdf = f(rayorig + (t + (u + 1) * d / (ticks - 1)) * raydir);
    if (sign(sdf) != signAtRayOrigin) {
      return t + u * d / (ticks - 1);
    }
  }
  return t;
}

bool expectSeverelyNonlinearDistance = false;
vec4 raymarch(vec3 rayorig, vec3 raydir) {
  int step = 0;
  float d = 0.0001;
  float signAtRayOrigin = sign(f(rayorig));
  float t;

  for (t = d; step < renderDepth && d > 0.00001; t += d) {
    float sdf = f(rayorig + t * raydir);
    
    // This is a moderately brute-force workaround to handle non-linear distance functions.
    // If the sign of the function has changed, we know that f() must have overestimated
    // and we've overshot into the nearest surface.  So we rewind and step along the
    // ray instead, in three progressively finer intervals (so, 30 extra steps max).
    if (expectSeverelyNonlinearDistance && sign(sdf) != signAtRayOrigin) {
      t = findInterval(rayorig, raydir, signAtRayOrigin, t - d, d, 10);
      t = findInterval(rayorig, raydir, signAtRayOrigin, t, d / 10.0, 10);
      t = findInterval(rayorig, raydir, signAtRayOrigin, t, d / 100.0, 10);
      return vec4(rayorig + t * raydir, float(step));
    }
    d = abs(sdf) * 0.99;
    step++;
  }

  return vec4(rayorig + t * raydir, float(step));
}

vec3 renderScene(vec3 rayorig, vec3 raydir) {
  TBD tbd[10];
  int numTbd = 0;
  vec3 cumulativeColor = vec3(0);

  raydir = normalize(raydir);
  
  tbd[numTbd++] = TBD(rayorig, raydir, 1.0);
  for (int i = 0; i < 10 && numTbd > 0; i++) {
    vec3 src = tbd[numTbd - 1].src;
    vec3 dir = normalize(tbd[numTbd - 1].dir);
    float weight = tbd[numTbd - 1].weight;
    numTbd--;

    vec4 res = raymarch(src, dir);
    if (res.w < renderDepth) {
      vec3 pt = res.xyz;
      SdfMaterial material = scene(pt);
      vec3 normal = normalize(material.gradient);
      float illumination = (0.25 + shadow(pt)) * illuminate(pt, normal, src, lightPos);
      int normalSign = int(sign(dot(normal, dir)));

      cumulativeColor += weight * material.mat.base * illumination * material.mat.color;
      if (material.mat.refractive > 0) {
        vec3 refractNormal = normalSign * normal;
        vec3 refractPt = pt + 2 * max(0.0001, abs(material.sdf)) * refractNormal;
        vec3 refractDir = refract(dir, -refractNormal, 
            (normalSign < 0) ? (REFRACTIVE_INDEX_OF_AIR / material.mat.refractiveIndex) : (material.mat.refractiveIndex / REFRACTIVE_INDEX_OF_AIR));
        tbd[numTbd++] = TBD(refractPt, refractDir, weight * material.mat.refractive);
      }
      if (material.mat.reflective > 0) {
        tbd[numTbd++] = TBD(pt + 0.001 * normal, reflect(dir, normal), weight * material.mat.reflective);
      }
    } else {
      cumulativeColor += weight * getBackground(dir);
    }
  }
   return cumulativeColor;
}
