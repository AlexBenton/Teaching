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

float shadow(vec3 pt) {
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

vec4 raymarch(vec3 rayorig, vec3 raydir) {
  int step = 0;
  float d = 0.0001;
  vec3 pt;
  float signAtRayOrigin = sign(f(rayorig));

  for (float t = d; step < renderDepth && d > 0.00001; t += d) {
    pt = rayorig + t * raydir;
    float sdf = f(pt);
    
    // This is a slightly arcane workaround to handle non-linear distance functions.
    // If the sign of the function has changed, we know that f() must have lied to us
    // about the distance to the nearest surface.  So we rewind and step along the
    // ray instead.
    if (sign(sdf) != signAtRayOrigin) {
      t -= d;
      int ticks = 10;
      for (float u = 0; u < ticks; u++) {
        sdf = f(rayorig + (t + (u + 1) * d / (ticks - 1)) * raydir);
        if (sign(sdf) != signAtRayOrigin) {
          pt = rayorig + (t + u * d / (ticks - 1)) * raydir;
          return vec4(pt, float(step));
        }
      } 
    }
    d = abs(sdf) * 0.99;
    step++;
  }

  return vec4(pt, float(step));
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
