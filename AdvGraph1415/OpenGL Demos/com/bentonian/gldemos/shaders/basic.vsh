#version 330

uniform mat4 modelToScreen;

in vec4 vPosition;

void main() {
  gl_Position = modelToScreen * vPosition;
}
