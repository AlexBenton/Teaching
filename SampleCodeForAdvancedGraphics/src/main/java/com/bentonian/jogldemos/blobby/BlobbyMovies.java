package com.bentonian.jogldemos.blobby;

import javax.media.opengl.GLAutoDrawable;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.metaballs.ImplicitSurface;
import com.bentonian.framework.mesh.metaballs.MetaBall;
import com.bentonian.framework.ui.GLImageBufferCanvas;
import com.bentonian.jogldemos.internals.JoglDemo;

public class BlobbyMovies extends JoglDemo {

  private static final M3d RED = new M3d(1,0,0);
  private static final M3d BLUE = new M3d(0,0,1);
  
  private int frame = 0;
  private ImplicitSurface surface;
  private MetaBall mover;

  public BlobbyMovies() {
    super("Blobby Movies");
    setCameraDistance(12);
  }

  @Override
  public void init(GLAutoDrawable glDrawable) {
    super.init(glDrawable);

    surface = new ImplicitSurface(new M3d(-10,-10,-10), new M3d(10,10,10))
        .setTargetLevel(5)
        .addForce(mover = new MetaBall(0,0,0,1.0,RED))
        .addForce(new MetaBall(-4,0,0,1.0,BLUE));
  }

  @Override
  public void draw() {
    mover.setX(4 * Math.cos((frame / 500.0) * 2 * 3.141592));
    surface.reset();
    surface.refine();
    
    surface.render(this);
    
    if (frame < 500) {
      frame++;
      new GLImageBufferCanvas(getWidth(), getHeight()).write(gl, "output/Frame" + frame + ".PNG");
    }
  }
}
