#version 330

#include "common.fsh"
#include "noise3D.fsh"

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

float f(vec3 pt) {
  float f = sdCube(pt, vec3(1));
  f = min(f, octahedron(pt - vec3(-3, 3, 0)));
  f = min(f, dodecahedron(pt - vec3(3, 3, 0)));
  f = min(f, icosahedron(pt - vec3(-3, -3, 0)));
  f = min(f, truncatedIcosahedron(pt - vec3(3, -3, 0)));
  return f;
}

float fWithPlane(vec3 pt) {
  return min(f(pt), sdPlane(pt - vec3(0, 0, sin(iGlobalTime)), vec4(0,0,1,0)));
}

vec3 gradient(vec3 pt) {
  vec3 off = vec3(0.0001, 0, 0);
  return vec3(
    fWithPlane(pt + off.xyz) - fWithPlane(pt - off.xyz),
    fWithPlane(pt + off.yxz) - fWithPlane(pt - off.yxz),
    fWithPlane(pt + off.yzx) - fWithPlane(pt - off.yzx));
}

vec4 raymarch(vec3 rayorig, vec3 raydir) {
  int step = 0;
  vec3 pos = rayorig; 
  float d = fWithPlane(pos);
  int sign = (d < 0) ? -1 : 1;

  while (abs(d) > 0.001 && step < renderDepth) {
    pos = pos + raydir * sign * d;
    d = fWithPlane(pos);
    step++;
  }

  return vec4(pos, float(step));
}

vec3 illuminate(vec3 pt, vec3 rayorig) {
  vec3 normal = normalize(gradient(pt));
  float diff = diffuse(pt.xyz, normal, lightPos);
  vec3 color = (abs(pt.z - sin(iGlobalTime)) < 0.01) ? getDistanceColor(pt, f(pt))  : white;
  return (0.25 + diff) * color;
}

vec3 scene(vec3 rayorig, vec3 raydir) {
  vec4 res = raymarch(rayorig, raydir);  
  return (res.w < renderDepth)
      ? illuminate(res.xyz, rayorig)
      : background;
}

void main() {
  fragColor = vec4(scene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
