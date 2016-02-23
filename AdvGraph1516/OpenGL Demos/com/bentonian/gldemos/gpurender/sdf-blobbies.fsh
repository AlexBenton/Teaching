#version 330

#include "include/common.fsh"

uniform bool iShowRenderDepth;

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

vec3 forces[3] = vec3[3](
  5 * vec3(1, 0, 0),
  5 * vec3(-1, 0, 0),
  5 * vec3(cos(iGlobalTime), 0, 0)
);

float sdImplicitSurface(vec3 p) {
  float mb = 0;
  float minDist = 10000;
  for (int i = 0; i < forces.length(); i++) {
    mb += getMetaball(p, forces[i]);
    minDist = min(minDist, length(p - forces[i]));
  }
  if (minDist > b) {
    return max (minDist - b, b - 1.2679529);
  } else if (mb == 0) {
    return b - 1.2679529;  // 1.2679529 is the x-intercept of the metaball expression - 0.5.
  } else {
    return b - sqrt(6 * mb) - 1.2679529;
  }
}

float getSdf(vec3 p) {
  return sdImplicitSurface(p);
}

float getSdfWithPlane(vec3 p) {
  return min(getSdf(p), sdPlane(p, vec4(0,1,0,1)));
}

vec4 raymarch(vec3 rayorig, vec3 raydir) {
  int step = 0;
  vec3 pos = rayorig;
  float d = getSdfWithPlane(pos);

  while (abs(d) > 0.001 && step < renderDepth) {
    pos = pos + raydir * d;
    d = getSdfWithPlane(pos);
    step++;
  }

  return vec4(pos, float(step));
}

float getShadow(vec3 pt) {
  vec3 lightDir = normalize(lightPos - pt);
  float kd = 1;
  int step = 0;

  for (float t = 0.1; t < length(lightPos - pt) && step < renderDepth && kd > 0.001; ) {
    float d = getSdf(pt + t * lightDir);
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

vec3 shade(vec3 pt, vec3 rayorig) {
  vec3 color = (abs(pt.y + 1) < 0.001) ? getDistanceColor(pt, getSdf(pt)) : white;
  vec3 normal = normalize(GRADIENT(pt, getSdfWithPlane));
  return color * illuminate(pt, normal, rayorig, lightPos) * (0.25 + getShadow(pt));
}

vec3 scene(vec3 rayorig, vec3 raydir) {
  vec4 res = raymarch(rayorig, raydir);

  return iShowRenderDepth
      ? vec3(float(res.w) / 50.0)
      : (res.w < renderDepth)
          ? shade(res.xyz, rayorig)
          : background;
}

void main() {
  fragColor = vec4(scene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
