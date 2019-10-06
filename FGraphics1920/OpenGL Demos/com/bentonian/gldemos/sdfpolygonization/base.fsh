#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "signed distance functions.fsh"
#include "rendering.fsh"
#include "raymarching.fsh"

float fScene(vec3 pt);
float f(vec3 pt) {
  float a = fScene(pt);   // User-generated function
return a;  
  float b = pt.y;         // XZ plane
  return min(a, b);
}

SdfMaterial scene(vec3 pt) {
  bool isFloor = abs(pt.y) <= 0.00001;
  vec3 normal = isFloor ? vec3(0, 1, 0) : GRADIENT(pt, f);
  Material mat = isFloor 
      ? Material(getFloorColor(pt, fScene(pt)), 1, 0, 0, 1) 
      : Material(white, 0.9, 0, 0.1, 1);

  return SdfMaterial(f(pt), normal, mat);
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}

//###########################################################################//

