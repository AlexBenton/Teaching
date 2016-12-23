#version 330

#include "include/common.fsh"
#include "include/signed distance functions.fsh"

const int renderDepth = 400;
const vec3 lightPos = vec3(5, 5, 10);

uniform bool iShowRenderDepth;  // Re-purposed to toggle ambient occlusion

float cube(vec3 p, float radius) {
  vec3 b = vec3(radius);
  vec3 d = abs(p) - b;
  return min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0)) - 0.01;
}

float getSdf(vec3 p) {
  float f = cube(p - vec3(0, 0.1, -1), 1);
  f = min(f, cube(p - vec3(0, 0.1, -1) - vec3(0, 1.6, 0), 0.5));
  f = min(f, sdSphere(p - vec3(0, 0.1, 1), 1));
  return f;
}

float getSdfWithPlane(vec3 p) {
  return min(getSdf(p), max(length(p) - 5, sdPlane(p, vec4(0,1,0,1))));
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

float getAmbientOcclusion(vec3 pt, vec3 normal) {
  float a = 1;
  int step = 0;
  
  for (float t = 0.01; t <= 0.1; ) {
    float d = abs(getSdfWithPlane(pt + t * normal));
    a = min(a, d / t);
    t += max(d, 0.01);
  }
  return a;
}

vec3 shade(vec3 pt, vec3 rayorig) {
  vec3 color = (abs(pt.y + 1) < 0.001 && (fract(pt.x) < 0.05 || fract(pt.z) < 0.05)) ? gray : white;
  vec3 normal = normalize(GRADIENT(pt, getSdfWithPlane));
  float ambient = iShowRenderDepth ? 1 : getAmbientOcclusion(pt, normal);
  
  return color * (0.25 * ambient + 0.75 * illuminate(pt, normal, rayorig, lightPos));
}

vec3 scene(vec3 rayorig, vec3 raydir) {
  vec4 res = raymarch(rayorig, raydir);

  return (res.w < renderDepth)
      ? shade(res.xyz, rayorig)
      : getBackground(raydir);
}

void main() {
  fragColor = vec4(scene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
