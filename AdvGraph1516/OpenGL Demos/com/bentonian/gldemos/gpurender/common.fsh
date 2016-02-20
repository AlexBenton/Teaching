// common.fsh
// 
// Standard include file for shader routines
// (Yes, there's no real '#include' in shaders)

uniform vec2 iResolution;
uniform float iGlobalTime;
uniform vec3 iRayOrigin;
uniform vec3 iRayDir;
uniform vec3 iRayUp;

in vec2 texCoord;
out vec4 fragColor;

const vec3 background = vec3(0.2, 0.2, 0.8);
const vec3 white = vec3(0.8, 0.8, 0.8);
const vec3 gray = vec3(0.4, 0.4, 0.4);
const vec3 black = vec3(0, 0, 0);
const vec3 red = vec3(0.8, 0.2, 0.2);
const vec3 green = vec3(0.2, 0.8, 0.2);
const vec3 blue = vec3(0.2, 0.2, 0.8);

vec3 getRayDir(vec3 camDir, vec3 camUp, vec2 texCoord) {
  vec3 xAxis = normalize(cross(camDir, camUp));
  vec2 p = 2.0 * texCoord - 1.0;
  p.x *= iResolution.x / iResolution.y;
  return normalize(p.x * xAxis + p.y * camUp + 5 * camDir);
}

// http://iquilezles.org/www/articles/distfunctions/distfunctions.htm
float sdCube(vec3 p, vec3 b) {
  vec3 d = abs(p) - b;
  return min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0)) - 0.1;
}

float sdSphere(vec3 p, float s) {
  return length(p) - s;
}

float sdTorus(vec3 p, vec2 t) {
  vec2 q = vec2(length(p.xz) - t.x,p.y);
  return length(q) - t.y;
}

float sdPlane(vec3 p, vec4 n) {
  return dot(p,n.xyz) + n.w;
}

// http://iquilezles.org/www/articles/smin/smin.htm
float smin( float a, float b) {
  float k = 0.2;
  float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0 );
  return mix( b, a, h ) - k * h * (1.0 - h);
}

// Blinn's metaballs
const float a = 1;
const float b = 3;
float getMetaball(vec3 p, vec3 v) {
  float r = length(p - v);
  if (r < b / 3) {
    return a * (1 - 3 * r * r / b * b);
  } else if (r < b) {
    return (3 * a / 2) * (1 - r / b) * (1 - r / b);
  } else {
    return 0;
  }
}

// Generalized Distance Functions - Akleman, Chen 1999
// http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.90.803&rep=rep1&type=pdf
vec3 loci[19] = vec3[19](
  vec3(1, 0, 0),                // n1
  vec3(0, 1, 0),                // n2
  vec3(0, 0, 1),                // n3
  vec3(0.577, 0.577, 0.577),    // n4
  vec3(-0.577, 0.577, 0.577),   // n5
  vec3(0.577, -0.577, 0.577),   // n6
  vec3(0.577, 0.577, -0.577),   // n7
  vec3(0.000, 0.357, 0.934),    // n8
  vec3(0.000, -0.357, 0.934),   // n9
  vec3(0.934, 0.000, 0.357),    // n10
  vec3(-0.934, 0.000, 0.357),   // n11
  vec3(0.357, 0.934, 0.000),    // n12
  vec3(-0.357, 0.934, 0.000),   // n13
  vec3(0.000, 0.851, 0.526),    // n14
  vec3(0.000, -0.851, 0.526),   // n15
  vec3(0.526, 0.000, 0.851),    // n16
  vec3(-0.526, 0.000, 0.851),   // n17
  vec3(0.851, 0.526, 0.000),    // n18
  vec3(-0.851, 0.526, 0.000)    // n19
);

float akleman(vec3 pt, int first, int last) {
  // The akleman distance function is non-linear, so we need to get "close enough"
  // to the surface before homing in
  if (length(pt) > 2) {
    return length(pt) - 1;
  } else {
    float sum = 0;
    for (int i = first; i <= last; i++) {
      sum += pow(abs(dot(loci[i - 1], pt)), 128.0);
    }
    return pow(sum, 1.0 / 128.0) - 1;
  }
}

float octahedron(vec3 pt) {
  return akleman(pt, 4, 7);
}

float dodecahedron(vec3 pt) {
  return akleman(pt, 14, 19);
}

float icosahedron(vec3 pt) {
  return akleman(pt, 4, 13);
}

float truncatedIcosahedron(vec3 pt) {
  return akleman(pt, 4, 19);
}

// Lighting

vec3 getDistanceColor(vec3 pt, float d) {
  vec3 color = mix(red, green, 0.5 + 0.5 * sin(d * 3.141592));
  if (fract(d) < 0.05) {
    color = mix(color, black, smoothstep(0, 0.05, fract(d)));
  } else if (fract(d) < 0.1) {
    color = mix(black, color, smoothstep(0.05, 0.1, fract(d)));
  }
  return color;
}

float diffuse(vec3 point, vec3 normal, vec3 lightPos) {
  return clamp(dot(normal, normalize(lightPos - point)), 0.0, 1.0);
}

float specular(vec3 point, vec3 normal, vec3 lightPos, vec3 eyePos) {
  vec3 l = normalize(lightPos - point);
  vec3 r = reflect(-l, normal);
  vec3 e = normalize(eyePos - point);
  return dot(l, normal) > 0 ? clamp(pow(dot(r, e), 16), 0.0, 1.0) : 0;
}
