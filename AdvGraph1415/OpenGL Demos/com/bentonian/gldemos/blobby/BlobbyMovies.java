package com.bentonian.gldemos.blobby;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.implicits.ImplicitSurface;
import com.bentonian.framework.mesh.implicits.MetaBall;
import com.bentonian.framework.ui.BufferedImageRGBCanvas;
import com.bentonian.framework.ui.DemoApp;

public class BlobbyMovies extends DemoApp {

  private static final M3d RED = new M3d(1,0,0);
  private static final M3d BLUE = new M3d(0,0,1);
  
  private int frame = 0;
  private ImplicitSurface surface;
  private MetaBall mover;

  public BlobbyMovies() {
    super("Blobby Movies");
    this.surface = new ImplicitSurface(new M3d(-10,-10,-10), new M3d(10,10,10))
        .setTargetLevel(5)
        .addForce(mover = new MetaBall(0,0,0,1.0,RED))
        .addForce(new MetaBall(-4,0,0,1.0,BLUE));
    setCameraDistance(12);
  }

  @Override
  public void draw() {
    mover.setX(4 * Math.cos((frame / 500.0) * 2 * 3.141592));
    surface.reset();
    surface.refine();
    
    surface.render(this);
    
    if (frame < 500) {
      frame++;
      BufferedImageRGBCanvas
          .copyOpenGlContextToImage(getWidth(), getHeight())
          .write("output/Frame" + frame + ".PNG");
    }
  }
}
