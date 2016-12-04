#version 330

#include "include/common.fsh"
#define PI 3.1415926535897932384626433832795

const vec3 lightPos = vec3(0, 10, 10);

uniform sampler2D texture;

////////////////////////////////////////////////////////////////////

struct Hit {
  vec3 pt;
  vec3 normal;
  float t;
};

struct SceneElement {
  vec3 pos;
  float radius;
  Material mat;
};

////////////////////////////////////////////////////////////////////

SceneElement SCENE[4] = SceneElement[](
  SceneElement(vec3(sin(iGlobalTime), 0, 1.732 - 0.577) * 2, 1, Material(white, 0.5, 0.4, 0.1, REFRACTIVE_INDEX_OF_AIR + 0.01)),
  SceneElement(vec3(-1, 0, -0.577) * 2, 1, Material(red, 0.7, 0, 0.3, REFRACTIVE_INDEX_OF_AIR + 0.01)),
  SceneElement(vec3(1, 0, -0.577) * 2, 1, Material(blue, 0.7, 0, 0.3, REFRACTIVE_INDEX_OF_AIR + 0.01)),
  SceneElement(vec3(0, -101, 0), 100, Material(gray, 0.7, 0, 0.3, 0))
);

////////////////////////////////////////////////////////////////////

Hit traceSphere(vec3 rayorig, vec3 raydir, vec3 pos, float radius) {
  float OdotD = dot(rayorig - pos, raydir);
  float OdotO = dot(rayorig - pos, rayorig - pos);
  float base = OdotD * OdotD - OdotO + radius * radius;

  if (base >= 0) {
    float bm4ac = sqrt(base);
    float t1 = -OdotD + bm4ac;
    float t2 = -OdotD - bm4ac;
    if (t1 >= 0 || t2 >= 0) {
      float t = (t1 < t2 && t1 >= 0) ? t1 : t2;
      vec3 pt = rayorig + raydir * t;
      vec3 normal = normalize(pt - pos);
      return Hit(pt, normal, t);
    }
  }
  return Hit(vec3(0), vec3(0), -1);
}

vec3 getBackground(vec3 dir) {
  float u = 0.5 + atan(dir.z, -dir.x) / (2 * PI);
  float v = 0.5 - asin(dir.y) / PI;
  vec4 texColor = texture2D(texture, vec2(u, v));
  return texColor.rgb;
}

vec3 shade(vec3 color, Hit hit, vec3 rayorig) {
  vec3 lit = color * (0.25 + illuminate(hit.pt, hit.normal, rayorig, lightPos));
  
  // Check for shadows
  for (int i = 0; i < SCENE.length(); i++) {
    if (traceSphere(hit.pt, normalize(lightPos - hit.pt), SCENE[i].pos, SCENE[i].radius).t >= 0) {
      return color * 0.2;
    }
  }

  return lit;
}

bool fireRay(vec3 rayorig, vec3 raydir, out Hit nearest, out Material mat) {
  nearest = Hit(vec3(0), vec3(0), -1);

  // Find nearest intersection
  for (int i = 0; i < SCENE.length(); i++) {
    Hit hit = traceSphere(rayorig, raydir, SCENE[i].pos, SCENE[i].radius);
    if ((hit.t >= 0) && ((nearest.t < 0) || (hit.t < nearest.t))) {
      nearest = hit;
      mat = SCENE[i].mat;
    }
  }
  
  // Make the floor a little more interesting
  if (mat.color == gray) {
    mat.color = getDistanceColor(length(nearest.pt));
  }
  
  return (nearest.t >= 0);
}

vec3 renderScene(vec3 rayorig, vec3 raydir) {
  TBD tbd[10];
  int numTbd = 0;
  vec3 cumulativeColor = vec3(0);

  tbd[numTbd++] = TBD(rayorig, raydir, 1.0);
  for (int i = 0; i < 10 && numTbd > 0; i++) {
    Material mat;
    Hit hit;  
    vec3 src = tbd[numTbd - 1].src;
    vec3 dir = tbd[numTbd - 1].dir;
    float weight = tbd[numTbd - 1].weight;
    numTbd--;

    bool succeeded = fireRay(src, dir, hit, mat);
    if (succeeded) {
      vec3 pt = hit.pt;
      vec3 normal = hit.normal;

      cumulativeColor += weight * mat.base * shade(mat.color, hit, src);
      if (mat.refractive > 0) {
        int normalSign = int(sign(dot(normal, dir)));
        vec3 refractNormal = normalSign * normal;
        vec3 refractPt = pt + 0.001 * refractNormal;
        vec3 refractDir = refract(dir, -refractNormal, 
            (normalSign < 0) ? (REFRACTIVE_INDEX_OF_AIR / mat.refractiveIndex) : (mat.refractiveIndex / REFRACTIVE_INDEX_OF_AIR));
        tbd[numTbd++] = TBD(refractPt, refractDir, weight * mat.refractive);
      }
      if (mat.reflective > 0) {
        tbd[numTbd++] = TBD(pt + 0.001 * normal, reflect(dir, normal), weight * mat.reflective);
      }
    } else {
      cumulativeColor += weight * getBackground(dir);
    }
  }
   return cumulativeColor;
}

void main() {
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
