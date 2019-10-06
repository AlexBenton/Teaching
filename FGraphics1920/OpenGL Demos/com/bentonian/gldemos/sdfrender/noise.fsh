#version 330

#include "include/common.fsh"
#include "include/noise3D.fsh"
#include "include/signed distance functions.fsh"

uniform bool iShowRenderDepth;

const int renderDepth = 200;
const vec3 lightPos = vec3(0, 10, 10);

float f(vec3 pt) {
  float noise = snoise(pt / 128) + snoise(pt / 32) + snoise(pt / 8);
  return noise + 0.7;
}

vec4 raymarch(vec3 rayorig, vec3 raydir) {
  int step = 0;
  vec3 pos = rayorig; 
  float d = f(pos);

  while (abs(d) > 0.001 && step < renderDepth) {
    pos = pos + raydir * abs(d);
    d = f(pos);
    step++;
  }

  return vec4(pos, float(step));
}

vec3 shade(vec3 pt, vec3 rayorig) {
  vec3 normal = normalize(GRADIENT(pt, f));
  vec3 color = white;
  if (dot(normal, rayorig - pt) < 0) {
    normal = -normal;
    color = green * 0.5;
  }
  if (abs(length(rayorig-pt) - 20) < 0.1) {
    color = red;
  }
  return color * (0.25 + illuminate(pt, normal, rayorig, lightPos));
}

vec3 scene(vec3 rayorig, vec3 raydir) {
  vec4 res = raymarch(rayorig + 20 * raydir, raydir);  
  return iShowRenderDepth
      ? vec3(float(res.w) / 50.0)
      : (res.w < renderDepth)
          ? shade(res.xyz, rayorig)
          : getBackground(raydir);
}

void main() {
  float dt = 3.141592 / 30.0;
  float t = iGlobalTime / 30.0;
  float r = 200;

  vec3 pos = r * vec3(cos(t), 0, sin(t));
  vec3 at = r * vec3(cos(t + dt), 0, sin(t + dt));

  fragColor = vec4(scene(pos, getRayDir(normalize(at - pos), vec3(0, 1, 0), texCoord)), 1.0);
}
