#version 330

///////////////////////////////////////////////////////////////////////////////
//
// With grateful thanks to these articles by Inigo Quilez:
//   http://iquilezles.org/www/articles/smin/smin.htm
//   http://iquilezles.org/www/articles/distfunctions/distfunctions.htm
// 

const int renderDepth = 400;
const vec3 lightPos = vec3(10, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

float fScene(vec3 pt) {

  // Scale 2x along X
  mat4 S = mat4(
      vec4(2, 0, 0, 0),
      vec4(0, 1, 0, 0),
      vec4(0, 0, 1, 0),
      vec4(0, 0, 0, 1));
  
  // Rotation in XY
  float t = sin(iGlobalTime) * PI / 4;
  mat4 R = mat4(
      vec4(cos(t),  sin(t), 0, 0),
      vec4(-sin(t), cos(t), 0, 0),
      vec4(0,       0,      1, 0),
      vec4(0,       0,      0, 1));

  // Translate to (3, 3, 3)
  mat4 T = mat4(
      vec4(1, 0, 0, 3),
      vec4(0, 1, 0, 3),
      vec4(0, 0, 1, 3),
      vec4(0, 0, 0, 1));
      
  pt = (vec4(pt, 1) * inverse(S * R * T)).xyz;

  return sdSphere(pt, 1);
}

float fPlane(vec3 pt) {
  float xz = sdPlane(vec3(pt), vec4(0, 1, 0, 0));
  float yz = sdPlane(vec3(pt), vec4(1, 0, 0, 0));
  float xy = sdPlane(vec3(pt), vec4(0, 0, 1, 0));
  return max(length(pt) - 8, 
      min(xz, min(yz, xy)));
}

float f(vec3 pt) {
  // Offset whole scene for better default camera positions
  pt = pt + vec3(3, 3, 3);
  
  float a = fScene(pt);
  float b = fPlane(pt);
  return min(a, b);
}

SdfMaterial scene(vec3 pt) {
  // Offset whole scene for better default camera positions
  pt = pt + vec3(3, 3, 3);
  
  float a = fScene(pt);
  float b = fPlane(pt);

  if (a < b) {
    return SdfMaterial(a, GRADIENT(pt, fScene), Material(white, 0.8, 0, 0.2, 1));
  } else {
    bool isGridline = (length(pt) < 8) && ((fract(pt.x) <= 0.05 ? 1 : 0) + (fract(pt.y) <= 0.05 ? 1 : 0) + (fract(pt.z) <= 0.05 ? 1 : 0)) >= 2;
    bool isAxis = isGridline && ((abs(pt.x - 3) <= 0.05 ? 1 : 0) + (abs(pt.y - 3) <= 0.05 ? 1 : 0) + (abs(pt.z - 3) <= 0.05 ? 1 : 0)) >= 1;
    vec3 color = isAxis ? red : isGridline ? gray : white;
    return SdfMaterial(b, GRADIENT(pt, fPlane), Material(color, 1, 0, 0, 1));
  }
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
