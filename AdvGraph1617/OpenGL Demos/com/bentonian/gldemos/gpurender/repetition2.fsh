#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

#define SPHERE_SIZE 1

const Material WHITE = Material(white, 1, 0, 0, 1);

float f(vec3 pt) {
  float ptLength = length(pt);
  float angle = atan(pt.z, pt.x);
  float radius = length(pt.xz);
  float radiusOfCenterOfRing = floor((radius + SPHERE_SIZE) / (SPHERE_SIZE * 2)) * (SPHERE_SIZE * 2);
  float circumferenceAtRing = 2 * PI * radiusOfCenterOfRing;
  float numSpheres = max(1, floor(circumferenceAtRing / (SPHERE_SIZE * 2)));
  float arcPerSphere = 2 * PI / numSpheres;
  float whichArc = mod(floor((angle + 2 * PI) / arcPerSphere), numSpheres);
  float arcMidAngle = whichArc * arcPerSphere + arcPerSphere / 2;
  vec3 pos = vec3(cos(arcMidAngle) * radiusOfCenterOfRing, 0, sin(arcMidAngle) * radiusOfCenterOfRing);

  pt = pt - vec3(0, sin(iGlobalTime - radiusOfCenterOfRing / (SPHERE_SIZE * 4)), 0);
  
  return max(ptLength - 39, sdSphere(pt - pos, SPHERE_SIZE));
}

SdfMaterial scene(vec3 pt) {
  float fVal = f(pt);
  return SdfMaterial(fVal, GRADIENT(pt, f), WHITE);
}

void main() {
  expectSeverelyNonlinearDistance = true;
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
