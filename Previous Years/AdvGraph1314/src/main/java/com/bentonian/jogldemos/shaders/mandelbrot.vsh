#version 330

uniform mat4 modelToScreen;

in vec4 vPosition;

out vec2 coordPosition;

void main() {
  coordPosition = (modelToScreen * vPosition).xy;
  gl_Position = modelToScreen * vPosition;
}
