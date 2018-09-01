#version 330

uniform mat3 normalToWorld;
uniform vec3 eyePosition;
uniform vec3 lightPosition;

in vec3 local;
in vec3 position;
in vec3 normal;

float scale = 10;
vec3 color = vec3(0.8, 0.8, 0.2);

out vec4 fColor;

void main() {
  bool lattice = 
      (fract(scale * local.x) > 0.9)
      || (fract(scale * local.y) > 0.9)
      || (fract(scale * local.z) > 0.9);
  if (!lattice) {
    discard;
  } else {
    vec3 l = normalize(lightPosition - position);
    vec3 r = normalize(reflect(-l, normal));
    vec3 v = normalize(eyePosition - position);
    float rDotV = clamp(dot(r, v), 0.0, 1.0);
    fColor = vec4(rDotV * color, 1);
  }
}
