#version 330

uniform vec3 lightPosition;

in vec3 position;
in vec3 normal;

out vec4 fragmentColor;

const vec3 YELLOW = vec3(1, 1, 0);
const vec3 BLACK = vec3(0.2, 0.2, 0.2);

const vec3 CENTER = vec3(0, 0, 1);
const vec3 LEFT_EYE = vec3(-0.2, 0.25, 0);
const vec3 RIGHT_EYE = vec3(0.2, 0.25, 0);
const mat3 EYE_SCALE = mat3(1, 0, 0, 0, 0.6, 0, 0, 0, 0);

void main() {
  vec3 n = normalize(normal);
  vec3 l = normalize(lightPosition - position);
  float illumination = 0.2 + 0.8 * clamp(0, dot(n, l), 1);

  bool isEye = (length(EYE_SCALE * position - LEFT_EYE) < 0.1)
      || (length(EYE_SCALE * position - RIGHT_EYE) < 0.1);
  bool isMouth = (length(position - CENTER) > 0.75 - 0.15 * abs(position.y))
      && (length(position - CENTER) < 0.75)
      && (position.y <= -0.1);
  bool isOutsideFace = (length(position - CENTER) > 1);
  vec3 color = (isEye || isMouth || isOutsideFace) ? BLACK : YELLOW;

  fragmentColor = vec4(color * illumination, 1.0);
}
