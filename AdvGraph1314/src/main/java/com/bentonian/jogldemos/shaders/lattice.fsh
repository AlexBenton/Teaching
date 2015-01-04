#version 330

uniform mat3 normalToWorld;
uniform vec3 eyePosition;
uniform vec3 lightDirection;

in vec3 vLocal;
in vec3 vWorld;
in vec3 nWorld;

float scale = 10;
vec3 color = vec3(0.8, 0.8, 0.2);

out vec4 fColor;

void main() {
  bool lattice = 
      (fract(scale * vLocal.x) > 0.9)
      || (fract(scale * vLocal.y) > 0.9)
      || (fract(scale * vLocal.z) > 0.9);
  if (!lattice) {
    discard;
  } else {
    vec3 l = normalize(lightDirection);
    vec3 r = normalize(reflect(-l, nWorld));
    vec3 v = normalize(eyePosition - vWorld);
    float rDotV = clamp(dot(r, v), 0.0, 1.0);
    fColor = vec4(rDotV * color, 1);
  }
}
