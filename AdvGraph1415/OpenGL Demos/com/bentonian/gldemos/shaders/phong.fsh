#version 330

uniform vec3 lightDirection;

in vec3 normal;

out vec4 fragmentColor;

vec3 color = vec3(0.2, 0.6, 0.8);

void main() {
  vec3 n = normalize(normal);
  float diff = clamp(dot(n, lightDirection), 0.2, 1.0);
  fragmentColor = vec4(color * diff, 1.0);
}
