#version 430

//#define ADAPTIVE
#define ADAPTIVE_SEGMENTS 25
#define ITERATIVE_STEPS 50
#define TOLERANCE 0.01
#define ENVELOPE_MARGIN 0.11

uniform vec2 iResolution;
uniform float iGlobalTime;
uniform vec3 seeds[4];

in vec2 texCoord;
out vec4 fragColor;

const vec3 white = vec3(0.9, 0.9, 0.9);
const vec3 gray = vec3(0.4, 0.4, 0.4);
const vec3 black = vec3(0, 0, 0);
const vec3 red = vec3(0.8, 0.2, 0.2);
const vec3 green = vec3(0.2, 0.8, 0.2);
const vec3 blue = vec3(0.2, 0.2, 0.8);

///////////////////////////////////////////////////////////////////////////////

bool isGridEdge(vec2 pt) {
  return fract(pt.x) < 0.01 || fract(pt.x) > 1 - 0.01 || fract(pt.y) < 0.01 || fract(pt.y) > 1 - 0.01;
}

bool isControlPoint(vec2 pt) {
  float d = 10000;
  for (int i = 0; i < 4; i++) {
    d = min(d, length(seeds[i].xy - pt));
  }
  return d < 0.2;
}

///////////////////////////////////////////////////////////////////////////////

struct Cubic {
  vec2 A, B, C, D;
};

// Distance from C to line segment AB
float distanceToLineSegment(vec2 A, vec2 B, vec2 C) {
  float t = dot(B - A, C - A) / pow(length(B - A), 2);
  return (t < 0) ? length(C - A) : (t > 1) ? length(C - B) : length(C - (A + t * (B - A)));
}

float distanceToBox(vec2 center, vec2 dim, vec2 pt) {
  vec2 d = abs(pt - center) - dim;
  return min(max(d.x, d.y), 0.0) + length(max(d, 0.0));
}

float distanceToCubicEnvelope(Cubic c, vec2 pt) {
  vec2 m = min(min(c.A, c.B), min(c.C, c.D));
  vec2 M = max(max(c.A, c.B), max(c.C, c.D));
  
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

vec2 evaluateCubic(in Cubic c, float t) {
  return pow(1-t, 3) * c.A 
      + 3 * t * pow(1-t, 2) * c.B
      + 3 * pow(t, 2) * (1-t) * c.C
      + pow(t, 3) * c.D;
}

float distanceAdaptive(in Cubic c, vec2 pt, out int steps) {
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

float distanceIterative(in Cubic c, vec2 pt) {
  float distance = 10000;
  vec2 curr = evaluateCubic(c, 0);
  for (int i = 1; i < ITERATIVE_STEPS; i++) {
    float t = float(i) / (ITERATIVE_STEPS - 1);
    vec2 next = evaluateCubic(c, t);
    distance = min(distance, distanceToLineSegment(curr, next, pt));
    curr = next;
  }
  return distance;
}

float distanceToCubic(in Cubic c, vec2 pt) {
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
  return Cubic(seeds[0].xy, seeds[1].xy, seeds[2].xy, seeds[3].xy);
}

///////////////////////////////////////////////////////////////////////////////

void main() {
  vec2 pt = 6 * vec2(
      (texCoord.x * 2 - 1) * iResolution.x / iResolution.y, 
      texCoord.y * 2 - 1);
  vec3 color = white;
  Cubic c = getCubic();

  if (isGridEdge(pt)) {
    color = gray;
  }
  if (distanceToCubic(c, pt) < 0.05) {
    color = black;
  }
  if (isControlPoint(pt)) {
    color = green;
  }
  
  fragColor = vec4(color, 1.0);
}
