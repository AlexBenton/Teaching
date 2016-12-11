#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

// Bug: highly visible artifacts appear as bend increases
float fScene(vec3 pt) {
  float t = (pt.y / 4.0 + 1) * PI / 2.0 * sin(iGlobalTime * 2.0 * PI / 10.0);
  vec3 pos = vec3(cos(t) * pt.x - sin(t) * pt.z, pt.y / 4.0, sin(t) * pt.x + cos(t) * pt.z);
  return sdCube(pos, vec3(1));
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
