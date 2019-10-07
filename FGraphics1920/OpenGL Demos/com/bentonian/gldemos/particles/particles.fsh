#version 330

in vec3 position;
in vec3 normal;
in vec3 color;

out vec4 fragmentColor;

const vec3[] LIGHT_DIR = vec3[3](
    normalize(vec3(1, 1, 1)),
    normalize(vec3(-1, 1, 1)),
    normalize(vec3(0, 1, 0)));

float weightNearZero(float f, float r) {
  return max(r - abs(fract(f + r) - r), 0.0) / r;
}

void main() {
  float diff = 0;
  vec3 c = color;
  if (color == vec3(0, 1, 0.8)) {
    diff = 1;
    c = mix(vec3(1, 0, 0), vec3(0, 0, 1), 1 - normal.x);
  } else {
    vec3 n = normalize(normal);
    if (position.y < 0.001) {
      float gridWeight = max(weightNearZero(position.x, 0.025), weightNearZero(position.z, 0.025));
      c = mix(vec3(1), vec3(0), gridWeight);
    }
    for (int i = 0; i < 3; i++) {
      diff += clamp(dot(n, LIGHT_DIR[i]), 0, 1.0);
    }
    diff = clamp(diff / 3.0, 0.2, 1.0);
  }
  fragmentColor = vec4(c * diff, 1);
}
