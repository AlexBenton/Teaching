#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/metaballs.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

vec4 forces[3] = vec4[](
  vec4(4, 0, 0, 1),
  vec4(-4, 0, 0, 1),
  vec4(4 * cos(iGlobalTime), 0, 0, 1)
);

float fSurface(vec3 pt) {
  return sdImplicitSurface(pt, forces);
}

float fPlane(vec3 pt) {
  return sdPlane(vec3(pt) - vec3(0, -1.5, 0), vec4(0, 1, 0, 0));
}

float f(vec3 pt) {
  float a = fSurface(pt);
  float b = fPlane(pt);
  return min(a, b);
}

SdfMaterial scene(vec3 pt) {
  float a = fSurface(pt);
  float b = fPlane(pt);

  if (a < b) {
    return SdfMaterial(a, GRADIENT(pt, fSurface), Material(white, vec4(0.5, 0.5, 0, REFRACTIVE_INDEX_OF_AIR + 0.01)));
  } else {
    return SdfMaterial(b, GRADIENT(pt, fPlane), Material(getDistanceColor(fSurface(pt)), vec4(1, 0, 0, 1)));
  }
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
