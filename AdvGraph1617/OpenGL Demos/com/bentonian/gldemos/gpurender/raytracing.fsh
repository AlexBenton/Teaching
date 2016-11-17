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
  vec3 color;
};

const SceneElement SCENE[4] = SceneElement[](
  SceneElement(vec3(0, 0, 1.732 - 0.577) * 2, 1, red),
  SceneElement(vec3(-1, 0, -0.577) * 2, 1, white),
  SceneElement(vec3(1, 0, -0.577) * 2, 1, blue),
  SceneElement(vec3(0, -101, 0), 100, gray)
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
      if (length(rayorig - pos) < length(pt - pos)) {
        normal = -normal;
      }
      return Hit(pt + normal * 0.001, normal, t);
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

vec3 scene(vec3 rayorig, vec3 raydir) {
  Hit nearest = Hit(vec3(0), vec3(0), -1);
  vec3 color;

  // Find nearest intersection
  for (int i = 0; i < SCENE.length(); i++) {
    Hit hit = traceSphere(rayorig, raydir, SCENE[i].pos, SCENE[i].radius);
    if ((hit.t >= 0) && ((nearest.t < 0) || (hit.t < nearest.t))) {
      nearest = hit;
      color = SCENE[i].color;
    }
  }
  
  // Make the colors a little more interesting
  if (color == gray) {
    color = getDistanceColor(length(nearest.pt));
  } else {
    color = color + getBackground(normalize(nearest.pt)) * 0.1;
  }
  
  return (nearest.t >= 0)
      ? shade(color, nearest, rayorig)
      : getBackground(raydir);
}

void main() {
  fragColor = vec4(scene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
