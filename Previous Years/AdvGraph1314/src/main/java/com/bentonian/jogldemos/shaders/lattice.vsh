#version 330

uniform mat4 modelToScreen;
uniform mat4 modelToWorld;
uniform mat3 normalToWorld;

in vec4 vPosition;
in vec3 vNormal;

out vec3 vLocal;
out vec3 vWorld;
out vec3 nWorld;

void main() {
  vLocal      = vec3(vPosition);
  vWorld      = vec3(modelToWorld * vPosition);
  nWorld      = vec3(normalToWorld * vNormal);
  gl_Position = modelToScreen * vPosition;
}
