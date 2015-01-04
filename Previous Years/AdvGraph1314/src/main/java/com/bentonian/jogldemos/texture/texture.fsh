#version 330

uniform vec3 lightDirection;
uniform sampler2D texture;
uniform bool enableTexturing;

in vec3 normal;
in vec2 texCoord;
in vec3 c;

out vec4 fragmentColor;

void main() {
  vec3 n = normalize(normal);
  float diff = clamp(dot(n, lightDirection), 0.2, 1.0);
  if (enableTexturing) {
    vec4 texColor = texture2D(texture, texCoord);
    fragmentColor = vec4(texColor.rgb * diff, texColor.a);
  } else {
    fragmentColor = vec4(c * diff, 1);
  }
}
