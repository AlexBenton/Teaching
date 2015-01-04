#version 330

uniform mat4 modelToCamera;
uniform mat4 modelToScreen;
uniform mat3 normalToCamera;

vec3 LightPosEyeCoords = vec3(0, 10, 4);

in vec4 vPosition;
in vec3 vNormal;

out vec3  P;
out vec3  N;
out vec3  L;

void main() {
  P               = vec3(modelToCamera * vPosition);
  N               = normalize(normalToCamera * vNormal);
  L               = normalize(LightPosEyeCoords - P);
  gl_Position     = modelToScreen * vPosition;
}
