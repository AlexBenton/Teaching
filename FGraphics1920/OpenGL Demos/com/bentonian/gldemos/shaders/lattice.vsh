#version 330

uniform mat4 modelToScreen;
uniform mat4 modelToWorld;
uniform mat3 normalToWorld;

in vec4 vPosition;
in vec3 vNormal;

out vec3 local;
out vec3 position;
out vec3 normal;

void main() {
  local       = vec3(vPosition);
  position    = vec3(modelToWorld * vPosition);
  normal      = vec3(normalToWorld * vNormal);
  gl_Position = modelToScreen * vPosition;
}
