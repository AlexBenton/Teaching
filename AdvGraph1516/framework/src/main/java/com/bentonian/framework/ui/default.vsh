#version 330

uniform mat4 modelToScreen;
uniform mat4 modelToWorld;
uniform mat3 normalToWorld;

in vec4 vPosition;
in vec3 vNormal;
in vec3 vColor;
in vec2 vTexCoord;

out vec3 position;
out vec3 normal;
out vec2 texCoord;
out vec3 color;

void main() {
  gl_Position = modelToScreen * vPosition;
  position = (modelToWorld * vPosition).xyz;
  if (length(vNormal) < 0.0001) {
    normal = vec3(0, 0, 0);
  } else {
    normal = normalToWorld * vNormal;
  }
  texCoord = vTexCoord;
  color = vColor;
}
