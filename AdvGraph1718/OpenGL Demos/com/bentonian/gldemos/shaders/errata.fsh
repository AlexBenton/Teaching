#version 330

in vec3 c;
out vec4 fragmentColor;

void main() {
  fragmentColor = vec4(c, 1);
}
