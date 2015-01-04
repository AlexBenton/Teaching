#version 330

uniform mat4 modelToScreen;
uniform mat3 normalToWorld;

in vec4 vPosition;
in vec3 vNormal;

out vec3 normal;

void main() {
  normal = normalize(normalToWorld * vNormal);
  gl_Position = modelToScreen * vPosition;
}
