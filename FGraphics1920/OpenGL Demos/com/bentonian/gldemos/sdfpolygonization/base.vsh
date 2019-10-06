#version 330

in vec4 vPosition;
in vec2 vTexCoord;

out vec2 texCoord;

void main() {
  gl_Position = vPosition;
  texCoord = vec2(vTexCoord.x, 1.0 - vTexCoord.y);
}
