#version 330

uniform vec2 Center;
uniform float NumSteps;
uniform float Zoom;

in vec2 coordPosition;

out vec4 fragmentColor;

void main() {
  vec2 z, c;

  c.x = 1.3333 * (-0.5 - coordPosition.x / Zoom) - Center.x;
  c.y = (coordPosition.y / Zoom) + Center.y;

  float i;
  z = c;
  for (i = 0.0; i<NumSteps; i++) {
      float x = (z.x * z.x - z.y * z.y) + c.x;
      float y = (z.y * z.x + z.x * z.y) + c.y;

      if((x * x + y * y) > 4.0) break;
      z.x = x;
      z.y = y;
  }

  if (i <= NumSteps) {
    float color = i / NumSteps;
    fragmentColor = vec4(color, color, 1.0, 1.0);
  } else {
    fragmentColor = vec4(NumSteps, 1.0, 1.0, 1.0);
  }
}