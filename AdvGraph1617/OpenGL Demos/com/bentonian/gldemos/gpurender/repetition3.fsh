#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

#define SPHERE_SIZE 1

const Material WHITE = Material(white, 1, 0, 0, 1);

float cube(vec3 p, float b) {
  vec3 d = abs(p) - vec3(b);
  return min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0)) - b / 100;
}

float plus(vec3 pt, float scale) {
  float f = cube(pt, scale / 3.0);
  float inflate = 1.01;
  f = min(f, cube(pt - vec3(scale * 2.0 / 3.0, 0, 0),  scale * inflate / 3.0));
  f = min(f, cube(pt - vec3(-scale * 2.0 / 3.0, 0, 0), scale * inflate / 3.0));
  f = min(f, cube(pt - vec3(0, scale * 2.0 / 3.0, 0),  scale * inflate / 3.0));
  f = min(f, cube(pt - vec3(0, -scale * 2.0 / 3.0, 0), scale * inflate / 3.0));
  f = min(f, cube(pt - vec3(0, 0, scale * 2.0 / 3.0),  scale * inflate / 3.0));
  f = min(f, cube(pt - vec3(0, 0, -scale * 2.0 / 3.0), scale * inflate / 3.0));
  return f;
}

float repeatedPlus(vec3 pt, float level) {
  float scale = 1 / pow(3, level);
  return plus(mod(pt + scale, scale * 2.0) - scale, scale);
}

float f(vec3 pt) {
  float f = cube(pt, 1);
  f = max(f, -repeatedPlus(pt, 0));
  f = max(f, -repeatedPlus(pt, 1));
  f = max(f, -repeatedPlus(pt, 2));
  f = max(f, -repeatedPlus(pt, 3));
  return f;
}

SdfMaterial scene(vec3 pt) {
  float fVal = f(pt);
  return SdfMaterial(fVal, GRADIENT(pt, f), WHITE);
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
