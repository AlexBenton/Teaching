#version 330

//
// Fragment shader for Gooch shading
//
// Author: Randi Rost
//
// Copyright (c) 2002-2005 3Dlabs Inc. Ltd. 
//
// See 3Dlabs-License.txt for license information
//

uniform vec3 vColor;

float DiffuseCool = 0.3;
float DiffuseWarm = 0.3;
vec3 CoolColor = vec3(0, 0, 0.6);
vec3 WarmColor = vec3(0.6, 0, 0);

in float NdotL;
in vec3  ReflectVec;
in vec3  ViewVec;

out vec4 fragmentColor;

void main()
{
  vec3 kcool    = min(CoolColor + DiffuseCool * vColor, 1.0);
  vec3 kwarm    = min(WarmColor + DiffuseWarm * vColor, 1.0); 
  vec3 kfinal   = mix(kcool, kwarm, NdotL);

  vec3 nreflect = normalize(ReflectVec);
  vec3 nview    = normalize(ViewVec);
  float spec    = pow(max(dot(nreflect, nview), 0.0), 32.0);

  if (gl_FrontFacing) {
    fragmentColor = vec4(min(kfinal + spec, 1.0), 1.0);
  } else {
    fragmentColor = vec4(0, 0, 0, 1);
  }
}
