#version 330

uniform vec3 vColor;
uniform bool bFront;

float DiffuseCool = 0.3;
float DiffuseWarm = 0.3;
vec3 CoolColor = vec3(0, 0, 0.6);
vec3 WarmColor = vec3(0.6, 0, 0);

in vec3  P;
in vec3  N;
in vec3  L;

out vec4 fragmentColor;

vec3 gooch() {
  vec3 R        = normalize(reflect(-L, N));
  float NdotL   = (dot(L, N) + 1.0) * 0.5;

  vec3 kcool    = min(CoolColor + DiffuseCool * vColor, 1.0);
  vec3 kwarm    = min(WarmColor + DiffuseWarm * vColor, 1.0); 
  vec3 kfinal   = mix(kcool, kwarm, NdotL);

  vec3 nreflect = normalize(R);
  vec3 nview    = normalize(-P);
  float spec    = pow(max(dot(nreflect, nview), 0.0), 32.0);

  return min(kfinal + spec, 1.0);
}

void main() {
  if (gl_FrontFacing && bFront) {
    fragmentColor = vec4(gooch(), 1);
  } else if (!gl_FrontFacing && !bFront) {
    fragmentColor = vec4(0, 0, 0, 1);
  } else {
    discard;
  }
}
