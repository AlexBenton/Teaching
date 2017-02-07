#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

// Unit cube
float cube(vec3 p) {
  vec3 d = abs(p);
  return max(d.x, max(d.y, d.z)) - 1;
}

// Unit sphere
float sphere(vec3 p) {
  return length(p) - 1;
}

// Scene function - all scene elements except the ground plane
float fScene(vec3 pt) {

  // Rotation in XZ
  float t = sin(iGlobalTime) * PI / 4;
  t = t * length(pt) / 10;
  mat4 R = mat4(
      vec4(cos(t),  0,       sin(t), 0),
      vec4(0,       1,       0,      0),
      vec4(-sin(t), 0,       cos(t), 0),
      vec4(0,       0,       0,      1));
  pt = (R * vec4(pt, 1)).xyz;

  vec3 pos = pt;
  pos.x = mod(pos.x + 2, 4) - 2;
  pos.z = mod(pos.z + 2, 4) - 2;
  float f = cube(pos);
  
  float g = sphere(pt / 20);
  
  return max(f, g);
}

// Scene function - ground plane gets its own colors
float fPlane(vec3 pt) {
  return pt.y - -1.5;
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
    return SdfMaterial(a, GRADIENT(pt, fScene), Material(white, 0.9, 0, 0.1, 1));
  } else {
    vec3 color = getDistanceColor(a) * 0.5 + 0.5;
    if (mod(pt.x, 1) < 0.1 || mod(pt.z, 1) < 0.1) {
      color = gray;
    }
    if (abs(pt.x) < 0.1 || abs(pt.z) < 0.1) {
      color = red;
    }
    return SdfMaterial(b, GRADIENT(pt, fPlane), Material(color, 1, 0, 0, 1));
  }
}

void main() {
  expectSeverelyNonlinearDistance = true;
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
