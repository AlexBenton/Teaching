#version 330

///////////////////////////////////////////////////////////////////////////////
//
// With grateful thanks to these articles by Inigo Quilez:
//   http://iquilezles.org/www/articles/smin/smin.htm
//   http://iquilezles.org/www/articles/distfunctions/distfunctions.htm
// 

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

float blend(float a, float b, float k) {
  float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
  return mix(b, a, h) - k * h * (1.0 - h);
}

float fScene(vec3 pt) {
  float a, b;
  
  a = sdCube(pt - vec3(-2, 0, -2), vec3(1));
  b = sdSphere(pt - vec3(-2, 1.5, -2), 1);
  float unioned = min(a, b);
  
  a = sdCube(pt - vec3(2, 0, -2), vec3(1));
  b = sdSphere(pt - vec3(2, 1.5, -2), 1);
  float intersected = max(a, b);
  
  a = sdCube(pt - vec3(2, 0, 2), vec3(1));
  b = sdSphere(pt - vec3(2, 1.5, 2), 1);
  float differenced = max(a, -b);
  
  a = sdCube(pt - vec3(-2, 0, 2), vec3(1));
  b = sdSphere(pt - vec3(-2, 1.5, 2), 1);
  float blended = blend(a, b, 0.2 + 0.5 + 0.5 * sin(iGlobalTime * 2.0 * PI / 10.0));

  return min(min(unioned, intersected), min(differenced, blended));
}

float fPlane(vec3 pt) {
  return max(length(pt) - 20, sdPlane(vec3(pt) - vec3(0, -1.5, 0), vec4(0, 1, 0, 0)));
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
