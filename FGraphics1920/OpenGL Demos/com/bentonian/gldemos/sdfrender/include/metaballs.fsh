// metaballs.fsh
//
// Include file for metaball implicit surfaces
// (Yes, there's no real '#include' in shaders)

////////////////////////////////////////////////////////////////////

// http://paulbourke.net/geometry/implicitsurf/
const float a = 1;
const float b = 3;
float getMetaball(vec3 p, vec3 v) {
  float r = length(p - v);
  return 1 / (r * r);
}

// Sum all forces, then bound to the max radius of the metaball function
float sdImplicitSurface(vec3 p, vec4[3] forces) {
  float mb = 0;
  for (int i = 0; i < forces.length; i++) {
    mb += getMetaball(p, forces[i].xyz) * forces[i].w;
  }
  return sqrt(1/mb) - 1;  
}
