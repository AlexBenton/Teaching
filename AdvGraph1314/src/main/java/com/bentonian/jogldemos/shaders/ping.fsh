#version 330

uniform vec3 Ping;
uniform float Time;
uniform vec3 eyePosition;
uniform vec3 lightDirection;

in vec3 worldPos;
in vec3 normal;

out vec4 fragmentColor;

vec3 specularColor = vec3(0, 0.5, 0.5);
vec3 diffuseColor = vec3(0.5, 0.5, 0);
float shininess = 2.0;
float kSpecular = 0.5;
float kDiffuse = 0.5;

#define PI 3.1415926535897932384626433832795
mat3 rotationMatrix(vec3 axis, float angle);

void main() {
  vec3 n = normalize(normal);

  if (length(Ping) > 0.01) {
    vec3 radial = worldPos - Ping;
    vec3 axisOfRotation = cross(radial, n);
    float d = length(radial);
    float theta = sin((d - Time) * PI * 5.0) * PI / 4.0  / ((d + 1) * (d + 1));
    mat3 tiltMatrix = rotationMatrix(axisOfRotation, theta);
    n = normalize(tiltMatrix * n);
  }

  vec3 r = normalize(reflect(lightDirection, n));
  vec3 e = normalize(eyePosition - worldPos);

  vec3 diffuse = diffuseColor * max(0.0, dot(n, lightDirection)) * kDiffuse;
  vec3 specular = specularColor * pow(max(0.0, dot(r, e)), shininess) * kSpecular;

  fragmentColor = vec4(diffuse + specular, 1);
}

mat3 rotationMatrix(vec3 axis, float angle)
{
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    axis = normalize(axis);
    return mat3(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c);
}
