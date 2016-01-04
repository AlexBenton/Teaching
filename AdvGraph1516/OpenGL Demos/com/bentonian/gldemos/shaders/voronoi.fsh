#version 330

uniform vec3 seeds[16];
uniform vec3 lightPosition;

in vec3 position;
in vec3 normal;

out vec4 fragmentColor;

const vec3 GRAY = vec3(0.1, 0.1, 0.1);

vec3 asColor(vec3 pos) {
  return (pos + vec3(1, 1, 1)) / 2;
}

// Inspired by http://nullprogram.com/blog/2014/06/01/
// Inspired by http://www.iquilezles.org/www/articles/voronoilines/voronoilines.htm
void main() {
  vec3 n = normalize(normal);
  float diffuse = max(0, dot(n, normalize(lightPosition - position)));
  vec3 color = (position + vec3(1, 1, 1)) / 2;

  if (length(seeds[0]) > 0.0001) {
    float dist = distance(seeds[0], position);
    vec3 pos = seeds[0];
    color = asColor(seeds[0]);

    for (int j = 1; j <= 16; j++) {
      int i = (j % 16);
      if (length(seeds[i]) > 0.0001) {
        float curr = distance(seeds[i], position);
        if (curr < dist) {
          vec3 midpt = (seeds[i] + pos) / 2;
          float t = dot(position - midpt, normalize(seeds[i] - pos));
          if (t < 0.01) {
            color = GRAY;
          } else {
            color = asColor(seeds[i]);
          }
          dist = curr;
          pos = seeds[i];
        }
      }
    }
  }
  
  for (int i = 0; i < 16; i++) {
    if (length(seeds[i]) > 0.0001 && distance(seeds[i], position) < 0.025) {
      color = GRAY;
    }
  }

  color = max(diffuse * color, GRAY);
  fragmentColor = vec4(color, 1.0);
}
