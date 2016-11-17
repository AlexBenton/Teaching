#version 330

#define PI 3.1415926535897932384626433832795

const vec3 BRICK = vec3(132.0 / 255.0, 31.0 / 255.0, 39.0 / 255.0);
const vec3 MORTAR = vec3(172.0 / 255.0, 160.0 / 255.0, 162.0 / 255.0);

uniform vec3 eyePosition;
uniform vec3 lightPosition;

in vec3 position;
in vec3 normal;

out vec4 fragmentColor;

mat3 rotationMatrix(vec3 axis, float angle);

void main() {
  vec3 pt = normalize(position);
  float u = 0.5 + atan(pt.z, -pt.x) / (2 * PI);
  float v = 0.5 - asin(pt.y) / PI;
  vec3 color;
  vec3 n = normalize(normal);
  float a = 0.1, d = 0.3, s = 0.6;

  float tx = floor(10 * u);
  float ty = floor(10 * v);
  bool oddity = (mod(tx, 2.0) == mod(ty, 2.0));
  float distToHorizontal = 10 * v - ty - 0.05;
  float distToVertical = 10 * u - tx - 0.05 + (oddity ? 10 : 0);
  if (abs(distToVertical) < 0.05 && abs(distToVertical) < distToHorizontal) {
    // Vertical mortar
    vec3 axis = cross(cross(normal, vec3(0,1,0)), normal);
    float tilt = distToVertical * 2 * PI;
    n = rotationMatrix(axis, tilt) * n;
    color = MORTAR;
  } else if (abs(distToHorizontal) < 0.05) {
    // Horizontal mortar
    vec3 axis = cross(vec3(0,1,0), normal);
    float tilt = distToHorizontal * 2 * PI;
    n = rotationMatrix(axis, tilt) * n;
    color = MORTAR;
  } else {
    color = BRICK;
    d = 0.8;
    s = 0.1;
  }

  vec3 l = normalize(lightPosition - position);
  vec3 e = normalize(position - eyePosition);
  vec3 r = reflect(l, n);

  float ambient = a;
  float diffuse = d * clamp(0, dot(n, l), 1);
  float specular = s * pow(clamp(0, dot(e, r), 1), 2);

  fragmentColor = vec4(color * (ambient + diffuse + specular), 1.0);
}

// https://gist.github.com/neilmendoza/4512992
mat3 rotationMatrix(vec3 axis, float angle)
{
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;
    
    return mat3(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c);
}