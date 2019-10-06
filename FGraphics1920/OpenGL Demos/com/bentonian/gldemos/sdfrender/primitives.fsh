#version 330

#include "include/common.fsh"
#include "include/signed distance functions.fsh"

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

float f(vec3 pt) {
  float f = sdCube(pt, vec3(1));
  f = min(f, octahedron(pt - vec3(-3, 0, 3)));
  f = min(f, dodecahedron(pt - vec3(3, 0, 3)));
  f = min(f, icosahedron(pt - vec3(-3, 0, -3)));
  f = min(f, truncatedIcosahedron(pt - vec3(3, 0, -3)));
  f = min(f, sdTorus(pt - vec3(0, -1.05, 0), vec2(7.8, 0.1)));
  return f;
}

float getSdfWithPlane(vec3 pt) {
  return min(f(pt), max(length(vec3(pt.x, pt.y * 2, pt.z)) - 8, sdPlane(pt - vec3(0, -1.05, 0), vec4(0,1,0,0))));
}

vec4 raymarch(vec3 rayorig, vec3 raydir) {
  int step = 0;
  vec3 pos = rayorig; 
  float d = getSdfWithPlane(pos);
  int sign = (d < 0) ? -1 : 1;

  while (abs(d) > 0.001 && step < renderDepth) {
    pos = pos + raydir * sign * d;
    d = getSdfWithPlane(pos);
    step++;
  }

  return vec4(pos, float(step));
}

vec3 shade(vec3 pt, vec3 rayorig) {
  vec3 normal = normalize(GRADIENT(pt, getSdfWithPlane));
  vec3 color = (abs(pt.y + 1.05) < 0.01) ? getDistanceColor(f(pt)) : white;
  return color * (0.25 + illuminate(pt, normal, rayorig, lightPos));
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
