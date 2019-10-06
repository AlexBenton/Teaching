// common.fsh
// 
// Standard include file for shader routines
// (Yes, there's no real '#include' in shaders)

#define REFRACTIVE_INDEX_OF_AIR 1.000277
#define PI 3.1415926535897932384626433832795

uniform vec2 iResolution;
uniform float iGlobalTime;
uniform vec3 iRayOrigin;
uniform vec3 iRayDir;
uniform vec3 iRayUp;
uniform sampler2D texture;  // Background texture

in vec2 texCoord;
out vec4 fragColor;

const vec3 white = vec3(0.8, 0.8, 0.8);
const vec3 gray = vec3(0.4, 0.4, 0.4);
const vec3 black = vec3(0, 0, 0);
const vec3 red = vec3(0.8, 0.2, 0.2);
const vec3 green = vec3(0.2, 0.8, 0.2);
const vec3 blue = vec3(0.2, 0.2, 0.8);

////////////////////////////////////////////////////////////////////
// Common data structures for raymarching and raytracing

struct Material {
  vec3 color;
  float base;
  float refractive;
  float reflective;
  float refractiveIndex;
};

struct TBD {
  vec3 src;
  vec3 dir;
  float weight;
};

////////////////////////////////////////////////////////////////////
// Scene

vec3 getRayDir(vec3 camDir, vec3 camUp, vec2 texCoord) {
  vec3 xAxis = normalize(cross(camDir, camUp));
  vec2 p = 2.0 * texCoord - 1.0;
  p.x *= iResolution.x / iResolution.y;
  return normalize(p.x * xAxis + p.y * camUp + 5 * camDir);
}

vec3 getBackground(vec3 dir) {
  float u = 0.5 + atan(dir.z, -dir.x) / (2 * PI);
  float v = 0.5 - asin(dir.y) / PI;
  vec4 texColor = texture2D(texture, vec2(u, v));
  return texColor.rgb;
}

////////////////////////////////////////////////////////////////////
// Lighting

vec3 getDistanceColor(float d) {
  vec3 color = mix(red, green, 0.5 + 0.5 * sin(d * 3.141592));
  if (fract(d) < 0.05) {
    color = mix(color, black, smoothstep(0, 0.05, fract(d)));
  } else if (fract(d) < 0.1) {
    color = mix(black, color, smoothstep(0.05, 0.1, fract(d)));
  }
  return color;
}

float diffuse(vec3 pt, vec3 normal, vec3 light) {
  return clamp(dot(normal, normalize(light - pt)), 0.0, 1.0);
}

float specular(vec3 pt, vec3 normal, vec3 light, vec3 eye) {
  vec3 l = normalize(light - pt);
  vec3 r = reflect(-l, normal);
  vec3 e = normalize(eye - pt);
  return dot(l, normal) > 0 ? clamp(pow(dot(r, e), 16), 0.0, 1.0) : 0;
}

float illuminate(vec3 pt, vec3 normal, vec3 eye, vec3 light) {
  float diff = diffuse(pt, normal, light);
  float spec = specular(pt, normal, light, eye);
  return diff + spec;
}
