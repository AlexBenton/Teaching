#version 330

#include "common.fsh"
#include "noise3D.fsh"

#define GLASS 0
#define BOX 1
#define MIRROR 2

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

vec2 materialMin(float a, float matA, float b, float matB) {
  return (abs(a) < abs(b)) ? vec2(a, matA) : vec2(b, matB);
}

vec2 f(vec3 pt) {
  float s = sdSphere(pt - vec3(0, 0.5,0), 0.75);
  s = smin(s, sdCube(pt - vec3(0, -0.5, 0), vec3(0.5)));
  s = max(s, -sdSphere(pt - vec3(0, 0.5, 0), 0.7));
  s = max(s, -sdCube(pt - vec3(0, -0.5, 0), vec3(0.45)));
  s = max(s, -sdCube(pt - vec3(0, 1.75, 0), vec3(1)));
  
  float c = sdCube(pt - vec3(0, -1.5, 0), vec3(20, 0.25, 20));
  
  vec2 res = materialMin(s, GLASS, c, BOX);

  float t = sdCube(pt - vec3(0, 0, -5 + 2 * sin(iGlobalTime)), vec3(5, 1, 1));
  
  res = materialMin(res.x, res.y, t, MIRROR);
  
  return res;
}

vec3 gradient(vec3 pt) {
  vec3 off = vec3(0.0001, 0, 0);
  return vec3(
    f(pt + off.xyz).x - f(pt - off.xyz).x,
    f(pt + off.yxz).x - f(pt - off.yxz).x,
    f(pt + off.yzx).x - f(pt - off.yzx).x);
}

vec4 raymarch(vec3 rayorig, vec3 raydir) {
  int step = 0;
  vec3 pos = rayorig;
  float d = f(pos).x;

  while (abs(d) > 0.001 && step < renderDepth) {
    pos = pos + raydir * abs(d);
    d = f(pos).x;
    step++;
  }

  return vec4(pos, float(step));
}

// Returns RGB
vec3 color(int which, vec3 pt) {
  switch (which) {
    default:
    case BOX:
      int x = int(floor(pt.x));
      int y = int(floor(pt.y));
      int z = int(floor(pt.z));
      return (((x + y + z) & 0x01) == 0) ? black : white;
    case GLASS:
      return blue;
    case MIRROR:
      return white;
  }
}

// Returns; .x = base, .y = refract, .z = reflect, .w = refractive index
// Invariant: x + y + z = 1
// Color = base * material color + refract * refracted color + reflect * reflected color
vec4 material(int which) {
  switch (which) {
    case BOX:
      return vec4(1, 0, 0, 1);
    case GLASS:
      return vec4(0.5, 0.5, 0, 1.330);
    case MIRROR:
      return vec4(0.25, 0, 0.75, 1);
  }
}

float getShadow(vec3 pt) {
  vec3 lightDir = normalize(lightPos - pt);
  float kd = 1;
  int step = 0;

  for (float t = 0.1; t < length(lightPos - pt) && step < renderDepth && kd > 0.001; ) {
    float d = abs(f(pt + t * lightDir).x);
    if (d < 0.001) {
      kd = 0;
    } else {
      kd = min(kd, 16 * d / t);
    }
    t += d;
    step++;
  }
  return kd;
}

vec3 tbdSrc[10], tbdDir[10];
float tbdWeight[10];
int numTbd = 0;

void addTbd(vec3 pt, vec3 dir, float weight) {
  if (numTbd < tbdSrc.length() - 1) {
    tbdSrc[numTbd] = pt;
    tbdDir[numTbd] = dir;
    tbdWeight[numTbd] = weight;
    numTbd++;
  }
}

void addRefract(vec3 pt, vec3 dir, vec3 normal, float refractiveIndexRatio, float weight) {
  vec3 refractDir = refract(dir, normal, refractiveIndexRatio);
  addTbd(pt, refractDir, weight);
}

vec3 scene(vec3 rayorig, vec3 raydir) {
  vec3 cumulativeColor = vec3(0);
vec2 store;
  addTbd(rayorig, raydir, 1.0);
  for (int i = 0; i < 10 && numTbd > 0; i++) {
    vec3 src = tbdSrc[numTbd - 1];
    vec3 dir = tbdDir[numTbd - 1];
    float weight = tbdWeight[numTbd - 1];
    numTbd--;

    vec4 res = raymarch(src, dir);
    if (res.w < renderDepth) {
      vec3 pt = res.xyz;
      vec2 d = f(pt);
      int which = int(d.y);
      vec4 mat = material(which);
      vec3 normal = normalize(gradient(pt));
      bool comingFromOutside = sign(dot(normal, dir)) < 0;
      float illumination = (0.25 + getShadow(pt)) * illuminate(pt, normal, src, lightPos);

      cumulativeColor += weight * mat.x * illumination * color(which, pt);
      if (mat.y > 0) {
        addRefract(
            pt + 2 * max(0.001, abs(d.x)) * sign(dot(normal, dir)) * normal, 
            dir, 
            -sign(dot(normal, dir)) * normal, 
            comingFromOutside ? (1.000277 / mat.w) : (mat.w / 1.000277),
            weight * mat.y);
      }
      if (mat.z > 0) {
        addTbd(pt + 0.001 * normal, reflect(dir, normal), weight * mat.z);
      }
    } else {
      cumulativeColor += weight * background;
    }
  }
   return cumulativeColor;
}

void main() {
  fragColor = vec4(scene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
