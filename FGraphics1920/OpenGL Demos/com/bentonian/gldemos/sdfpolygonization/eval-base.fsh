#version 330

#include "signed distance functions.fsh"

uniform sampler2D evalBuffer;

in vec2 texCoord;
out vec4 fragColor;

float fScene(vec3 pt);

float vec4ToE7ToFloat(vec4 v) {
  int e7 = ((int(v.x * 255) & 0xFF) << 24)
      | ((int(v.y * 255) & 0xFF) << 16)
      | ((int(v.z * 255) & 0xFF) << 8)
      | ((int(v.w * 255) & 0xFF) << 0);
  return e7 / 10000000.0;
}

// Caution: this gets inaccurate at larger numbers, like above 100
vec4 floatToE7ToVec4(float f) {
  int i = int(f * 10000000);
  float x = ((i >> 24) & 0xFF) / 255.0;
  float y = ((i >> 16) & 0xFF) / 255.0;
  float z = ((i >> 8) & 0xFF) / 255.0;
  float w = ((i >> 0) & 0xFF) / 255.0;
  
  return vec4(x, y, z, w);
}

void main() {
  ivec2 lookup = ivec2((gl_FragCoord.x - 0.5) * 3, gl_FragCoord.y);
  vec3 pt = vec3(
      vec4ToE7ToFloat(texelFetch(evalBuffer, lookup + ivec2(0, 0), 0)),
      vec4ToE7ToFloat(texelFetch(evalBuffer, lookup + ivec2(1, 0), 0)),
      vec4ToE7ToFloat(texelFetch(evalBuffer, lookup + ivec2(2, 0), 0)));
  float f = fScene(pt);

  fragColor = floatToE7ToVec4(f);
}

//###########################################################################//
