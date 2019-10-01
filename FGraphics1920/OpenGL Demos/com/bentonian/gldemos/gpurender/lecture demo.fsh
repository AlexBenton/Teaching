#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

// Unit cube
float cube(vec3 p) {
  vec3 d = abs(p) - vec3(1); // 1 = radius
  return min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0));
}

// Unit sphere
float sphere(vec3 p) {
  return length(p) - 1; // 1 = radius
}

float cylinderY(vec3 p) {
  return length(p.xz) - 1; // 1 = radius
}

float torus(vec3 p) {
  vec2 t = vec2(2, 0.5); // Major and minor radii
  vec2 q = vec2(
      length(p.xz) - t.x, p.y);
  return length(q) - t.y;
}

// Rotation in the XZ plane
vec3 rotateXZ(vec3 pt, float t) {
  mat3 R = mat3(
      vec3(cos(t),  0,       sin(t)),
      vec3(0,       1,       0     ),
      vec3(-sin(t), 0,       cos(t)));
  return R * pt;
}

// Scene function - all scene elements except the ground plane
float fScene(vec3 pt) {
  return sphere(pt);
}

// Ground plane gets its own colors
float fPlane(vec3 pt) {
  return pt.y - -1.5;
}

float f(vec3 pt) {
  float a = fScene(pt);
  float b = fPlane(pt);
  return min(a, b);
}

float weightNearZero(float f, float r) {
  return max(r - abs(fract(f + r) - r), 0.0) / r;
}

vec3 getFloorColor(vec3 pt, float d) {
  float gridWeight = max(weightNearZero(pt.x, 0.025), weightNearZero(pt.z, 0.025));
  float sdfIsocline = weightNearZero(d, 0.075);
  float distanceTaper = smoothstep(1.0, 0.0, (d - 3.0) / 3.0);
  float sdfIsoclineWeight = distanceTaper * sdfIsocline;
  
  if (sdfIsoclineWeight >= gridWeight) {
    return mix(white, blue, sdfIsoclineWeight);
  } else {
    return mix(white, black, gridWeight);
  }
}

SdfMaterial scene(vec3 pt) {
  float a = fScene(pt);
  float b = fPlane(pt);

  if (a < b) {
    return SdfMaterial(a, GRADIENT(pt, fScene), Material(white, 0.9, 0, 0.1, 1));
  } else {
    vec3 color = getFloorColor(pt, a);
    return SdfMaterial(b, GRADIENT(pt, fPlane), Material(color, 1, 0, 0, 1));
  }
}

void main() {
  expectSeverelyNonlinearDistance = true;
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
