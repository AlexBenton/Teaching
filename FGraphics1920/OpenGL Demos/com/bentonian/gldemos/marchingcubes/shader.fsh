#version 430

uniform vec2 iResolution;
uniform float iGlobalTime;
uniform sampler2D texture;
uniform float kScale;
uniform bool shadeCells;

uniform int screenZoom;
uniform int numSeeds;
uniform vec3 seeds[16];

in vec2 texCoord;
out vec4 fragColor;

const vec3 white = vec3(0.9, 0.9, 0.9);
const vec3 gray = vec3(0.4, 0.4, 0.4);
const vec3 black = vec3(0, 0, 0);
const vec3 red = vec3(0.8, 0.2, 0.2);
const vec3 green = vec3(0.2, 0.8, 0.2);
const vec3 blue = vec3(0.2, 0.2, 0.8);

vec2 pt = screenZoom * /* 0.55 * */vec2(
    (texCoord.x * 2 - 1) * iResolution.x / iResolution.y/* + 0.33*/, 
    texCoord.y * 2 - 1/* + 0.33*/);

// Jim Blinn's simplest blobby surface function
float fBlinn(float r) {
  return 1.0 / (r * r);
}

// return signed implicit surface, thresholded at f=1
// negative values are inside the surface
float getSurface(vec2 pt) {
  float f = 0;
  for (int i = 0; i < numSeeds; i++) {
    f += fBlinn(length(seeds[i].xy - pt));
  }
  return 1 - f;
}

// return distance to nearest control point
float getNearestControlPoint(vec2 pt) {
  float d = 10000;
  for (int i = 0; i < numSeeds; i++) {
    d = min(d, length(seeds[i].xy - pt));
  }
  return d;
}

bool isGridEdge(vec2 pt) {
  return fract(pt.x) < 0.01 || fract(pt.x) > 1 - 0.01 || fract(pt.y) < 0.01 || fract(pt.y) > 1 - 0.01;
}

bool isCellEdge(vec2 pt) {
  float r = 0.01;
  return mod(pt.x, kScale) < r || mod(pt.x, kScale) > kScale - r || mod(pt.y, kScale) < r || mod(pt.y, kScale) > kScale - r;
}

bool isCorner(vec2 pt) {
  return length(vec2(kScale / 2) - mod(pt + vec2(kScale / 2), kScale)) < 0.075;
}

// Vertices are indexed clockwise from upper left
vec2 getCorner(vec2 pt, int index) {
  switch (index % 4) {
  case 0: return pt - mod(pt, kScale) + vec2(0, kScale);
  case 1: return pt - mod(pt, kScale) + vec2(kScale, kScale);
  case 2: return pt - mod(pt, kScale) + vec2(kScale, 0);
  case 3: return pt - mod(pt, kScale) + vec2(0, 0);
  }
}

// Edges are indexed clockwise from top
vec2 getWhereSurfaceCrossesEdgeApproximately(vec2 pt, int edgeIndex) {
  vec2 A = getCorner(pt, edgeIndex);
  vec2 B = getCorner(pt, edgeIndex + 1);
  float fA = getSurface(A);
  float fB = getSurface(B);
  float t = (0 - fA) / (fB - fA);
  return A + t * (B - A);
}

float getDistanceFromPtToLineBetweenEdges(vec2 pt, int firstEdge, int secondEdge) {
  vec2 A = getWhereSurfaceCrossesEdgeApproximately(pt, firstEdge);
  vec2 B = getWhereSurfaceCrossesEdgeApproximately(pt, secondEdge);
  
  if (dot(A - B, A - pt) < 0) {
    return length(A - pt);
  } else if (dot(B - A, B - pt) < 0) {
    return length(B - pt);
  } else {
    vec2 d = B - A;
    vec2 dPerp = normalize(vec2(d.y, -d.x));
    return abs(dot(dPerp, A - pt));
  }
}

float getDistanceToNearestPolygonizedEdge(vec2 pt) {
  float tl = getSurface(getCorner(pt, 0));
  float tr = getSurface(getCorner(pt, 1));
  float br = getSurface(getCorner(pt, 2));
  float bl = getSurface(getCorner(pt, 3));
  int flags = ((tl >= 0) ? 1 : 0) | ((tr >= 0) ? 2 : 0) | ((br >= 0) ? 4 : 0) | ((bl >= 0) ? 8 : 0);

  switch (flags) {
  case 1: return getDistanceFromPtToLineBetweenEdges(pt, 3, 0);
  case 2: return getDistanceFromPtToLineBetweenEdges(pt, 0, 1);
  case 3: return getDistanceFromPtToLineBetweenEdges(pt, 1, 3);
  case 4: return getDistanceFromPtToLineBetweenEdges(pt, 1, 2);
  case 5: return min(
      getDistanceFromPtToLineBetweenEdges(pt, 0, 3),
      getDistanceFromPtToLineBetweenEdges(pt, 1, 2));
  case 6: return getDistanceFromPtToLineBetweenEdges(pt, 0, 2);
  case 7: return getDistanceFromPtToLineBetweenEdges(pt, 2, 3);
  case 8: return getDistanceFromPtToLineBetweenEdges(pt, 2, 3);
  case 9: return getDistanceFromPtToLineBetweenEdges(pt, 0, 2);
  case 10: return min(
      getDistanceFromPtToLineBetweenEdges(pt, 0, 1),
      getDistanceFromPtToLineBetweenEdges(pt, 2, 3));
  case 11: return getDistanceFromPtToLineBetweenEdges(pt, 1, 2);
  case 12: return getDistanceFromPtToLineBetweenEdges(pt, 1, 3);
  case 13: return getDistanceFromPtToLineBetweenEdges(pt, 0, 1);
  case 14: return getDistanceFromPtToLineBetweenEdges(pt, 0, 3);
  }
  return 1000;
}

bool isInterestingCell(vec2 pt) {  
  float tl = getSurface(getCorner(pt, 0));
  float tr = getSurface(getCorner(pt, 1));
  float br = getSurface(getCorner(pt, 2));
  float bl = getSurface(getCorner(pt, 3));
  int flags = ((tl >= 0) ? 1 : 0) | ((tr >= 0) ? 2 : 0) | ((br >= 0) ? 4 : 0) | ((bl >= 0) ? 8 : 0);

  if (flags != 0 && flags != 15) {
    vec2 t = textureSize(texture, 0);
    ivec2 tc = ivec2(floor(pt.x / kScale) + t.x / 2, floor(pt.y / kScale) + t.y / 2); 
    float z = texelFetch(texture, tc, 0).z;
    return z > 0 && iGlobalTime / 5.0 >= z;
  } else {
    return false;
  }
}

bool isInterestingCorner(vec2 corner) {
  for (int i = -1; i <= 0; i++) {
    for (int j = -1; j <= 0; j++) {
      if (isInterestingCell(pt + kScale * vec2(i + 0.5, j + 0.5))) {
        return true;
      }
    }
  }
  return false;
}

vec2 getNearestCorner(vec2 pt) {
  return getCorner(pt, 3) + kScale * vec2(round(fract(pt.x / kScale)), round(fract(pt.y / kScale)));  
}

vec3 getInterestingCellColor(vec2 pt) {
  vec2 t = textureSize(texture, 0);
  ivec2 tc = ivec2(floor(pt.x / kScale) + t.x / 2, floor(pt.y / kScale) + t.y / 2); 
  return mix(red, blue, texelFetch(texture, tc, 0).z);
}

void main() {
  float d = getNearestControlPoint(pt);
  float s = getSurface(pt);
  bool interestingCell = isInterestingCell(pt);
  vec3 color = isGridEdge(pt) ? gray : white;
  
  if (s < -0.01) {
    color = color * 0.25 + green * 0.25 + white * 0.5;
  }
  
  if (d < 0.2) {
    color = green;
  }
  
  if (abs(s) <= 0.01) {
    color = green;
  }
  
  if (isCorner(pt) && !shadeCells) {
    if (isInterestingCorner(pt)) {
      color = (getSurface(getNearestCorner(pt)) < 0) ? red : blue;
    }
  }
  
  if (interestingCell) {
    if (getDistanceToNearestPolygonizedEdge(pt) < 0.025) {
      color = black;
    } else  if (isCellEdge(pt)) {
      color = black;
    } else if (shadeCells) {
      color = color * 0.5 + 0.5 * getInterestingCellColor(pt);
    }
  }
  
  fragColor = vec4(color, 1.0);
}
