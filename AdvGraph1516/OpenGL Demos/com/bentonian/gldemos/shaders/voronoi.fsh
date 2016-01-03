#version 330

uniform vec3 seeds[16];
uniform vec3 lightPosition;

in vec3 position;
in vec3 normal;

out vec4 fragmentColor;

const vec3 GRAY = vec3(0.1, 0.1, 0.1);

// Inspired by http://nullprogram.com/blog/2014/06/01/
void main() {
  vec3 n = normalize(normal);
  float diffuse = max(0, dot(n, normalize(lightPosition - position)));

  float dist = 100000;
  vec3 color = (position + vec3(1, 1, 1)) / 2;
  for (int i = 0; i < 16; i++) {
    if (length(seeds[i]) > 0.0001) {
      float curr = distance(seeds[i], position);
      if (curr < dist) {
        vec3 seedcolor = (seeds[i] + vec3(1, 1, 1)) / 2;
        if (dist - curr < 0.025) {
          color = GRAY;
        } else {
          color = seedcolor;
        }
        dist = curr;
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
