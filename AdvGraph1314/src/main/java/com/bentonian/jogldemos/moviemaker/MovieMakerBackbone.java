package com.bentonian.jogldemos.moviemaker;

import com.bentonian.jogldemos.moviemaker.scenes.BlobbyScene;
import com.bentonian.jogldemos.moviemaker.scenes.BunnyScene;
import com.bentonian.jogldemos.moviemaker.scenes.DifferenceScene;
import com.bentonian.jogldemos.moviemaker.scenes.IntersectionScene;
import com.bentonian.jogldemos.moviemaker.scenes.MorphScene;
import com.bentonian.jogldemos.moviemaker.scenes.RefractionScene;
import com.bentonian.jogldemos.moviemaker.scenes.ShadowedSphereScene;
import com.bentonian.jogldemos.moviemaker.scenes.TextureScene;
import com.bentonian.jogldemos.moviemaker.scenes.TwistScene;
import com.bentonian.jogldemos.moviemaker.scenes.WoodTextureEvolutionScene;


public class MovieMakerBackbone  {

  private static final MovieMakerScene[] SCENES = {
    new BunnyScene(),
    new MorphScene(),
    new TextureScene(),
    new WoodTextureEvolutionScene(),
    new DifferenceScene(),
    new IntersectionScene(),
    new RefractionScene(),
    new TwistScene(),
    new BlobbyScene(),
    new ShadowedSphereScene(),
  };

  private final MovieMakerEditor editor;
  private final MovieMakerRayTracer raytracer;
  private final MovieMakerAnimator animator;

  private int currentScene;

  public MovieMakerBackbone() {
    this.editor = new MovieMakerEditor(this);
    this.raytracer = new MovieMakerRayTracer(this);
    this.animator = new MovieMakerAnimator(this);
    setScene(0);
  }

  public void rayTrace() {
    if (!raytracer.isVisible()) {
      raytracer.setVisible(true);
      raytracer.setLocation((int) editor.getFrameSize().getWidth(), 0);
      editor.requestFocus();
    }
    raytracer.render();
  }

  public void previousScene() {
    setScene((currentScene - 1 + SCENES.length) % SCENES.length);
  }

  public void nextScene() {
    setScene((currentScene + 1) % SCENES.length);
  }

  private void setScene(int scene) {
    animator.stop();
    animator.clearMovieElements();
    getScene().tearDown(this);

    currentScene = scene;

    animator.setAnimationName(getScene().getName());
    animator.setAutoReverse(getScene().getAutoReverse());
    animator.setLoop(getScene().getLoop());
    animator.setCaptureRayTracer(getScene().getCaptureRayTracer());
    raytracer.getRayTracer().setBackground(getScene().getBackground());
    raytracer.getRayTracer().setNumShadowRays(getScene().getNumShadowRays());
    raytracer.getRayTracer().setSupersamplingMultiple(getScene().getSupersamplingMultiple());
    getScene().setup(this);
  }

  public void animate() {
    animator.animate();
  }

  public MovieMakerEditor getEditor() {
    return editor;
  }

  public MovieMakerRayTracer getRayTracer() {
    return raytracer;
  }

  public MovieMakerAnimator getAnimator() {
    return animator;
  }

  public MovieMakerScene getScene() {
    return SCENES[currentScene];
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    final MovieMakerBackbone backbone = new MovieMakerBackbone();

    backbone.getEditor().setImageSize(640, 480);
    backbone.getRayTracer().setImageSize(480, 360);

    new Thread() {
      @Override
      public void run() {
        while (true) {
          backbone.getAnimator().preDisplay();
          backbone.getEditor().update();
          backbone.getRayTracer().update();
          backbone.getAnimator().postDisplay();
        }
      }
    }.start();
  }
}
