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
//     Material scene(vec3 pt);
// 
//   3. Call renderScene().  Ex:
//     fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);

////////////////////////////////////////////////////////////////////

struct Material {
  float sdf;
  vec3 gradient;
  vec3 color;
  vec4 mat;  // .x = base, .y = refract, .z = reflect, .w = refractive index
};

struct TBD {
  vec3 src;
  vec3 dir;
  float weight;
};

////////////////////////////////////////////////////////////////////
// Forward declarations, must be implemented

// Returns the SDF (signed distance field) at pt
float f(vec3 pt);

// Returns all material traits at pt.  (Computed, probably, from f(pt).)
Material scene(vec3 pt);

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
  vec3 pos = rayorig;
  float d = f(pos);

  while (abs(d) > 0.001 && step < renderDepth) {
    pos = pos + raydir * abs(d);
    d = f(pos);
    step++;
  }

  return vec4(pos, float(step));
}

vec3 renderScene(vec3 rayorig, vec3 raydir) {
  TBD tbd[10];
  int numTbd = 0;
  vec3 cumulativeColor = vec3(0);

  tbd[numTbd++] = TBD(rayorig, raydir, 1.0);
  for (int i = 0; i < 10 && numTbd > 0; i++) {
    vec3 src = tbd[numTbd - 1].src;
    vec3 dir = tbd[numTbd - 1].dir;
    float weight = tbd[numTbd - 1].weight;
    numTbd--;

    vec4 res = raymarch(src, dir);
    if (res.w < renderDepth) {
      vec3 pt = res.xyz;
      Material material = scene(pt);
      vec3 normal = normalize(material.gradient);
      float illumination = (0.25 + shadow(pt)) * illuminate(pt, normal, src, lightPos);
      int normalSign = int(sign(dot(normal, dir)));

      cumulativeColor += weight * material.mat.x * illumination * material.color;
      if (material.mat.y > 0) {
        vec3 refractNormal = normalSign * normal;
        vec3 refractPt = pt + 2 * max(0.001, abs(material.sdf)) * refractNormal;
        vec3 refractDir = refract(dir, -refractNormal, (normalSign < 0) ? (1.000277 / material.mat.w) : (material.mat.w / 1.000277));
        tbd[numTbd++] = TBD(refractPt, refractDir, weight * material.mat.y);
      }
      if (material.mat.z > 0) {
        tbd[numTbd++] = TBD(pt + 0.001 * normal, reflect(dir, normal), weight * material.mat.z);
      }
    } else {
      cumulativeColor += weight * background;
    }
  }
   return cumulativeColor;
}
