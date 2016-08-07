#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/noise3D.fsh"
#include "include/raymarching.fsh"

float fGlass(vec3 pt) {
  float s = sdSphere(pt - vec3(0, 0.5,0), 0.75);
  s = smin(s, sdCube(pt - vec3(0, -0.5, 0), vec3(0.5)));
  s = max(s, -sdSphere(pt - vec3(0, 0.5, 0), 0.7));
  s = max(s, -sdCube(pt - vec3(0, -0.5, 0), vec3(0.45)));
  s = max(s, -sdCube(pt - vec3(0, 1.75, 0), vec3(1)));
  return s;
}

float fFloor(vec3 pt) {
  return sdCube(pt - vec3(0, -1.5, 0), vec3(20, 0.25, 20));
}

float fMirror(vec3 pt) {
  return sdCube(pt - vec3(0, 0, -5 + 2 * sin(iGlobalTime)), vec3(5, 1, 1));
}

float f(vec3 pt) {
  float s = fGlass(pt);
  float c = fFloor(pt);
  float t = fMirror(pt);
  return min(s, min(c, t));
}

Material scene(vec3 pt) {
  float s = fGlass(pt);
  float c = fFloor(pt);
  float t = fMirror(pt);

  if (s < min(c, t)) {
    return Material(s, GRADIENT(pt, fGlass), blue, vec4(0.5, 0.5, 0, 1.330));
  } else if (c < min(s, t)) {
    return Material(c, 
        GRADIENT(pt, fFloor),
        ((int(floor(pt.x) + floor(pt.y) + floor(pt.z)) & 0x01) == 0) ? black : white,
        vec4(1, 0, 0, 1));
  } else {
    return Material(t, GRADIENT(pt, fMirror), white, vec4(0.25, 0, 0.75, 1));
  }
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
