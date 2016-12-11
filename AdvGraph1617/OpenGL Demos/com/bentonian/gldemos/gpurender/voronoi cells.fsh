#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

vec3 forces[4] = vec3[](
  vec3(4, 0, 0),
  vec3(-4, 0, 0),
  vec3(4 * cos(iGlobalTime), 2, 2 * sin(iGlobalTime)),
  vec3(2 * cos(iGlobalTime * 2), 2, 4 * sin(iGlobalTime * 2))
);

int findNearestForce(vec3 pt, int excluding) {
  int best = -1;
  float minDist = 10000;

  for (int i = 0; i < forces.length; i++) {
    if (i != excluding) {
      float d = length(pt - forces[i]);
      if (d < minDist) {
        minDist = d;
        best = i;
      }
    }
  }
  return best;
}

float fVoronoi(vec3 pt) {
  int a = findNearestForce(pt, -1);
  int b = findNearestForce(pt, a);
  vec3 n = normalize(forces[a] - forces[b]);
  vec3 p = (forces[a] + forces[b]) / 2;
  float dist = abs(dot((pt - p), n)) - 0.05;
  return max(sdSphere(pt, 5), dist);
}

float fGeneratingPoints(vec3 pt) {
  float dist = sdSphere(pt - forces[0], 0.1);
  for (int i = 1; i < forces.length; i++) {
    dist = min(dist, sdSphere(pt - forces[i], 0.1));
  }
  return dist;
}

float fPlane(vec3 pt) {
  return sdPlane(pt - vec3(0, -1.5, 0), vec4(0, 1, 0, 0));
}

float f(vec3 pt) {
  float a = fVoronoi(pt);
  float b = fGeneratingPoints(pt);
  float c = fPlane(pt);
  return min(a, min(b, c));
}

SdfMaterial scene(vec3 pt) {
  float a = fVoronoi(pt);
  float b = fGeneratingPoints(pt);
  float c = fPlane(pt);

  if (a < b && a < c) {
    return SdfMaterial(a, GRADIENT(pt, fVoronoi), Material(white, 0.5, 0.5, 0, REFRACTIVE_INDEX_OF_AIR + 0.01));
  } else if (b < c) {
    return SdfMaterial(a, GRADIENT(pt, fGeneratingPoints), Material(white, 1, 0, 0, 1));
  } else {
    return SdfMaterial(b, GRADIENT(pt, fPlane), Material(getDistanceColor(a), 1, 0, 0, 1));
  }
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
