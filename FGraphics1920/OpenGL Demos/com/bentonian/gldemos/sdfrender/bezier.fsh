#version 330

const int renderDepth = 400;
const vec3 lightPos = vec3(0, 10, 10);

#include "include/common.fsh"
#include "include/signed distance functions.fsh"
#include "include/raymarching.fsh"

#define ADAPTIVE
#define ADAPTIVE_SEGMENTS 25
#define ITERATIVE_STEPS 50
#define TOLERANCE 0.01
#define ENVELOPE_MARGIN 0.11

struct Cubic {
  vec3 A, B, C, D;
};

// Distance from C to line segment AB
float distanceToLineSegment(vec3 A, vec3 B, vec3 C) {
  float t = dot(B - A, C - A) / pow(length(B - A), 2);
  return (t < 0) ? length(C - A) : (t > 1) ? length(C - B) : length(C - (A + t * (B - A)));
}

float distanceToBox(vec3 center, vec3 dim, vec3 pt) {
  vec3 d = abs(pt - center) - dim;
  return min(max(d.x, max(d.y, d.z)), 0.0) + length(max(d, 0.0));
}

float distanceToCubicEnvelope(Cubic c, vec3 pt) {
  vec3 m = min(min(c.A, c.B), min(c.C, c.D));
  vec3 M = max(max(c.A, c.B), max(c.C, c.D));
  
  return distanceToBox((m+M)/2, (M-m)/2, pt);
}

bool isFlat(in Cubic c) {
  return distanceToLineSegment(c.A, c.D, c.B) <= TOLERANCE
      && distanceToLineSegment(c.A, c.D, c.C) <= TOLERANCE;
}

void subdivide(in Cubic c, out Cubic q, out Cubic r) {
  q.A = c.A;
  q.B = 0.5 * c.A + 0.5 * c.B;
  q.C = 0.25 * c.A + 0.5 * c.B + 0.25 * c.C;
  q.D = 0.125 * c.A + 0.375 * c.B + 0.375 * c.C + 0.125 * c.D;

  r.A = 0.125 * c.A + 0.375 * c.B + 0.375 * c.C + 0.125 * c.D;
  r.B = 0.25 * c.B + 0.5 * c.C + 0.25 * c.D;
  r.C = 0.5 * c.C + 0.5 * c.D;
  r.D = c.D;
}

vec3 evaluateCubic(in Cubic c, float t) {
  return pow(1-t, 3) * c.A 
      + 3 * t * pow(1-t, 2) * c.B
      + 3 * pow(t, 2) * (1-t) * c.C
      + pow(t, 3) * c.D;
}

float distanceAdaptive(in Cubic c, vec3 pt, out int steps) {
  float distance = 10000;
  int numSegments = 1;
  Cubic queue[ADAPTIVE_SEGMENTS];

  steps = 0;
  queue[0] = c;
  for (int i = 0; i < numSegments; i++) {
    steps++;
    if (isFlat(queue[i]) || numSegments >= ADAPTIVE_SEGMENTS - 1) {
      distance = min(distance, distanceToLineSegment(queue[i].A, queue[i].D, pt));
    } else {
      Cubic q,r;
      float e;
      
      subdivide(queue[i], q, r);
      e = distanceToCubicEnvelope(q, pt);
      if (e <= ENVELOPE_MARGIN) {
        queue[numSegments++] = q;
      } else {
        distance = min(distance, e);
      }
      e = distanceToCubicEnvelope(r, pt);
      if (e <= ENVELOPE_MARGIN) {
        queue[numSegments++] = r;
      } else {
        distance = min(distance, e);
      }
    }
  }
  return distance;
}

float distanceIterative(in Cubic c, vec3 pt) {
  float distance = 10000;
  vec3 curr = evaluateCubic(c, 0);
  for (int i = 1; i < ITERATIVE_STEPS; i++) {
    float t = float(i) / (ITERATIVE_STEPS - 1);
    vec3 next = evaluateCubic(c, t);
    distance = min(distance, distanceToLineSegment(curr, next, pt));
    curr = next;
  }
  return distance;
}

float distanceToCubic(in Cubic c, vec3 pt) {
  float envelope = distanceToCubicEnvelope(c, pt);
  if (envelope > ENVELOPE_MARGIN) {
    return envelope;
  }

#ifdef ADAPTIVE
  int steps;
  return distanceAdaptive(c, pt, steps);
#else
  return distanceIterative(c, pt);
#endif
}

Cubic getCubic() {
  float t = iGlobalTime / 10;
  return Cubic(
      vec3(3 * cos(t), 0, 3 * sin(t)),
      vec3(-3 + 3 * cos(t * 2), 0, 3 * sin(t * 2)),
      vec3(3 + 3 * cos(t * 3), 0, -3 * sin(t * 3)),
      vec3(3 * cos(t * 4), 0, 3 * sin(t * 4)));
}

float fScene(vec3 pt) {
  Cubic c = getCubic();
  float f = distanceToCubic(c, pt) - 0.1;
  f = min(f, sdSphere(pt - c.A, 0.2));
  f = min(f, sdSphere(pt - c.B, 0.2));
  f = min(f, sdSphere(pt - c.C, 0.2));
  f = min(f, sdSphere(pt - c.D, 0.2));
  return f;
}

float fPlane(vec3 pt) {
  return max(length(pt) - 20, sdPlane(vec3(pt) - vec3(0, -0.25, 0), vec4(0, 1, 0, 0)));
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
#ifdef ADAPTIVE
    int steps;
    Cubic c = getCubic();

    distanceAdaptive(c, pt, steps);
    float cost = float(steps) / ADAPTIVE_SEGMENTS;
    vec3 color = mix(vec3(0,0,1), vec3(1,0,0), smoothstep(0.2, 0.8, cost));
#else
    vec3 color = vec3(0,0,0.8);
#endif

    return SdfMaterial(a, GRADIENT(pt, fScene), Material(color, 0.8, 0, 0.2, 1));
  } else {
    bool track = min(abs(length(pt) - 3), min(abs(length(pt - vec3(-3,0,0)) - 3), abs(length(pt - vec3(3,0,0)) - 3))) < 0.01;
    bool gridline = min(fract(pt.x), fract(pt.z)) < 0.1;
    vec3 ground = track ? vec3(0) : gridline ? vec3(0.2) : vec3(0.8); 
    return SdfMaterial(b, vec3(0,1,0), Material(ground, 1, 0, 0, 1));
  }
}

void main() {
  enableShadows = false;
  fragColor = vec4(renderScene(iRayOrigin, getRayDir(iRayDir, iRayUp, texCoord)), 1.0);
}
