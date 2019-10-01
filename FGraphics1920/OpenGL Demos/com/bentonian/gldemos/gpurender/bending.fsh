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

float fScene(vec3 pt) {
  pt.y -= 1;
  float t = (pt.y + 2.5) * sin(iGlobalTime * 2 * PI / 10.0) * PI / 8;
  return sdCube(vec3(
      pt.x * cos(t) - pt.z * sin(t), 
	  pt.y / 2, 
	  pt.x * sin(t) + pt.z * cos(t)), vec3(1)) - 0.25;
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
  expectSeverelyNonlinearDistance = true;
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
