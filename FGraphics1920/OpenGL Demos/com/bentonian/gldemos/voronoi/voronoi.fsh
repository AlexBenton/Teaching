#version 330

uniform float iGlobalTime;
uniform vec3 seeds[16];
uniform vec3 lightPosition;

in vec3 position;
in vec3 normal;

out vec4 fragmentColor;

const vec3 GRAY = vec3(0.1, 0.1, 0.1);

float dist(vec3 a, vec3 b) {
  float f = distance(a, b);
//  if (a == seeds[0] || b == seeds[0]) {
//    f /= 1.5 + 0.5 * sin(iGlobalTime);
//  }
  return f;
}

vec3 asColor(vec3 pos) {
  return (pos + vec3(1, 1, 1)) / 2;
}

vec3 chooseNearest(vec3 pos, vec3 one, vec3 two) {
  return dist(pos, one) < dist(pos, two) ? one : two;
}

vec3 chooseFarthest(vec3 pos, vec3 one, vec3 two) {
  return dist(pos, one) >= dist(pos, two) ? one : two;
}

// Inspired by http://nullprogram.com/blog/2014/06/01/
// Inspired by http://www.iquilezles.org/www/articles/voronoilines/voronoilines.htm
bool findNearest(vec3 pos, out vec3 a, out vec3 b) {
  if (length(seeds[0]) <= 0.0001 || length(seeds[1]) <= 0.0001) {
    return false;
  }

  vec3 first = chooseNearest(pos, seeds[0], seeds[1]);
  vec3 second = chooseFarthest(pos, seeds[0], seeds[1]);

  for (int i = 2; i < 16; i++) {
    if (length(seeds[i]) > 0.0001) {
      float curr = dist(pos, seeds[i]);
      if (curr < dist(pos, first)) {
        second = first;
        first = seeds[i];
      } else if (curr < dist(pos, second)) {
        second = seeds[i];
      }
    }
  }

  a = first;
  b = second;
  return true;
}

void main() {
  vec3 a, b;
  vec3 n = normalize(normal);
  float diffuse = max(0, dot(n, normalize(lightPosition - position)));
  vec3 color = (position + vec3(1, 1, 1)) / 2;

  if (findNearest(position, a, b)) {
    color = asColor(a);
//    color *= 1 + 0.4 * sin(40 * dist(position, a));
    if (abs(dist(position, a) - dist(position, b)) < 0.02) { 
      float t = dot(position - ((a + b) / 2), normalize(a - b));
      if (t < 0.01) {
        color = mix(GRAY, color, smoothstep(0, 0.01, t));
      }
    }
  }
  
  for (int i = 0; i < 16; i++) {
    if (length(seeds[i]) > 0.0001 && distance(seeds[i], position) < 0.025) {
      color = (i == 0) ? normalize(vec3(0.1,0,0) + GRAY) : GRAY;
    }
  }

  color = max(diffuse * color, GRAY);
  fragmentColor = vec4(color, 1.0);
}
