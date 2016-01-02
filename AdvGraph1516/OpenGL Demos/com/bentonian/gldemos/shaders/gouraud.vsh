#version 330

uniform mat4 modelToScreen;
uniform mat4 modelToWorld;
uniform mat3 normalToWorld;
uniform vec3 lightPosition;

in vec4 vPosition;
in vec3 vNormal;

out vec4 color;

const vec3 purple = vec3(0.2, 0.6, 0.8);

void main() {
  vec3 p = (modelToWorld * vPosition).xyz;
  vec3 n = normalize(normalToWorld * vNormal);
  vec3 l = normalize(lightPosition - p);
  float ambient = 0.2;
  float diffuse = 0.8 * clamp(0, dot(n, l), 1);

  color = vec4(purple * (ambient + diffuse), 1.0);
  gl_Position = modelToScreen * vPosition;
}
