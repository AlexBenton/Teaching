#version 330

uniform mat4 modelToScreen;
uniform mat4 modelToWorld;
uniform mat3 normalToWorld;

in vec4 vPosition;
in vec3 vNormal;

out vec3 worldPos;
out vec3 normal;

void main() {
  worldPos = (modelToWorld * vPosition).xyz;
  normal = normalToWorld * vNormal;
  gl_Position = modelToScreen * vPosition;
}
