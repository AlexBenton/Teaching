package com.bentonian.jogldemos.moviemaker.scenes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Circle;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.jogldemos.moviemaker.MovieMakerBackbone;
import com.bentonian.jogldemos.moviemaker.MovieMakerScene;
import com.bentonian.jogldemos.moviemaker.MovieMakerAnimator.StockMovieElement;

public class ShadowedSphereScene extends MovieMakerScene {

  public ShadowedSphereScene() {
    clearLights();
    addLight(new M3d(0, 20, 0));

    add(new Circle()
        .setReflectivity(0.25)
        .translate(new M3d(0, -1.5, 0)));
    add(new Sphere()
        .setReflectivity(0.25));
  }

  @Override
  public boolean getCaptureRayTracer() {
    return true;
  }
  
  @Override
  public M3d getBackground() {
    return SKY_BLUE;
  }
  
  @Override
  public int getNumShadowRays() {
    return 1;
  }

  @Override
  public void setup(final MovieMakerBackbone backbone) {
    backbone.getAnimator().addMovieElement(new StockMovieElement() {
      private int ticker;
      @Override
      public int getEndTick() {
        return 5;
      }
      @Override public void start() {
        this.ticker = 0;
      }
      @Override
      public void onTick(double t) {
        switch (ticker++) {
        case 0:
          backbone.getRayTracer().getRayTracer().setNumShadowRays(20);
          backbone.getRayTracer().getRayTracer().setLightRadius(1);
          break;
        case 1:
          write(backbone, "ShadowedSphere - 20 rays - radius 1");
          backbone.getRayTracer().getRayTracer().setNumShadowRays(20);
          backbone.getRayTracer().getRayTracer().setLightRadius(5);
          break;
        case 2:
          write(backbone, "ShadowedSphere - 20 rays - radius 5");
          backbone.getRayTracer().getRayTracer().setNumShadowRays(100);
          backbone.getRayTracer().getRayTracer().setLightRadius(1);
          break;
        case 3:
          write(backbone, "ShadowedSphere - 100 rays - radius 1");
          backbone.getRayTracer().getRayTracer().setNumShadowRays(100);
          backbone.getRayTracer().getRayTracer().setLightRadius(5);
          break;
        case 4:
          write(backbone, "ShadowedSphere - 100 rays - radius 5");
          backbone.getRayTracer().getRayTracer().setNumShadowRays(1);
          backbone.getRayTracer().getRayTracer().setLightRadius(0);
          break;
        }
      }
    });
  }
  
  private void write(final MovieMakerBackbone backbone, String filename) {
    BufferedImage img = new BufferedImage(297, 191, BufferedImage.TYPE_3BYTE_BGR);
    for (int x = 0; x <= 296; x++) {
      for (int y = 0; y <= 190; y++) {
        img.setRGB(x, y, backbone.getRayTracer().getCanvas().getRGB(171 + x, 267 + y));
      }
    }
    try {
      ImageIO.write(img, "png", new File(
          FileSystemView.getFileSystemView().getHomeDirectory() + "\\" + filename + ".png"));
    } catch (IOException exc) {
      exc.printStackTrace();
      System.exit(-1);
    }
  }
}
