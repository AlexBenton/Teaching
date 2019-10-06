#version 330

uniform mat4 modelToScreen;
uniform mat4 modelToWorld;

in vec4 vPosition;
in vec2 vTexCoord;

out vec2 texCoord;

void main() {
  gl_Position = modelToScreen * vPosition;
  texCoord = vec2(vTexCoord.x, 1.0 - vTexCoord.y);
}
