#version 330

uniform vec3 lightPosition;
uniform sampler2D texture;
uniform bool enableTexturing;

in vec3 position;
in vec3 normal;

out vec4 fragmentColor;

void main() {
  float diff;
  if (normal == vec3(0, 0, 0)) {
    diff = 1;
  } else {
    vec3 n = normalize(normal);
    vec3 l = normalize(lightPosition - position);
    diff = clamp(dot(n, l), 0.2, 1.0);
  }
  fragmentColor = vec4(vec3(1) * diff, 1);
}
