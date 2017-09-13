#version 330

uniform mat4 modelToScreen;

in vec4 vPosition;
out vec3 c;

void main() {
  c = vPosition.xyz;
  gl_Position = modelToScreen * vPosition;
}
