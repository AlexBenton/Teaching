package com.bentonian.jogldemos.moviemaker.scenes;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.metaballs.DemiMetaTorus;
import com.bentonian.framework.mesh.metaballs.ImplicitSurface;
import com.bentonian.framework.mesh.metaballs.MetaTorus;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;

public class MobiusBagelScene extends MovieMakerScene {

  public MobiusBagelScene() {
    MetaTorus torus = new DemiMetaTorus(0.65);
    ImplicitSurface stripImplicit = new ImplicitSurface(new M3d(-6,-2,-6), new M3d(6,2,6))
        .setTargetLevel(5)
        .addForce(torus)
        .setShowEdges(false);

    add(stripImplicit);
  }
}
