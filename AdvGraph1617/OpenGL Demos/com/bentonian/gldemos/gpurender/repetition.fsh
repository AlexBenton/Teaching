#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

float fScene(vec3 pt) {
  vec3 pos;
  pos = vec3(mod(pt.x + 2, 4) - 2, pt.y, mod(pt.z + 2, 4) - 2);  // Repeat in XZ plane only 
  pos = mod(pt + vec3(2), 4) - vec3(2);                          // Repeat in all directions
  return max(length(pt) - 9, sdCube(pos, vec3(1)));
}

float fPlane(vec3 pt) {
  return max(length(pt) - 10, sdPlane(vec3(pt) - vec3(0, -1.5, 0), vec4(0, 1, 0, 0)));
}

float f(vec3 pt) {
  float a = fScene(pt);
  float b = fPlane(pt);
  return min(a, b);
}

SdfMaterial scene(vec3 pt) {
  float a = fScene(pt);
  float b = fPlane(pt);

  if (a < b) {
    return SdfMaterial(a, GRADIENT(pt, fScene), Material(white, 0.8, 0, 0.2, 1));
  } else {
    return SdfMaterial(b, GRADIENT(pt, fPlane), Material(getDistanceColor(a), 1, 0, 0, 1));
  }
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
