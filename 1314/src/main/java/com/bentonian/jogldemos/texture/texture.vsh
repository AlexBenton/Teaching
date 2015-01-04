#version 330

uniform mat4 modelToScreen;
uniform mat3 normalToWorld;

in vec4 vPosition;
in vec3 vNormal;
in vec3 vColor;
in vec2 vTexCoord;

out vec3 normal;
out vec2 texCoord;
out vec3 c;

void main() {
  gl_Position = modelToScreen * vPosition;
  normal = normalize(normalToWorld * vNormal);
  texCoord = vTexCoord;
  c = vColor;
}
