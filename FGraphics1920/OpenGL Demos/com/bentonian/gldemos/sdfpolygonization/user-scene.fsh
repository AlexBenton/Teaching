float fScene(vec3 pt) {
  return max(cube(pt), -sphere(pt - vec3(0, 0, 1)));
}
