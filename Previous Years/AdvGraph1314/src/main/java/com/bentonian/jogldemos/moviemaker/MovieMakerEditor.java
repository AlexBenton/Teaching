package com.bentonian.jogldemos.moviemaker;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.filechooser.FileSystemView;

import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.ui.GLBufferedImageCanvas;
import com.bentonian.jogldemos.internals.JoglDemo;


public class MovieMakerEditor extends JoglDemo {

  private static final String TITLE = "Movie Maker";
  private static final int WINDOW_PADDING_X = 16;
  private static final int WINDOW_PADDING_Y = 38;

  private final MovieMakerBackbone backbone;
  private final GLCanvas glCanvas;
  private final Frame frame;

  private GLBufferedImageCanvas imageCopy;

  public MovieMakerEditor(MovieMakerBackbone backbone) {
    super(TITLE);
    this.backbone = backbone;

    this.glCanvas = new GLCanvas();
    glCanvas.addGLEventListener(this);
    glCanvas.addKeyListener(this);
    glCanvas.addMouseListener(this);
    glCanvas.addMouseMotionListener(this);
    glCanvas.addMouseWheelListener(this);

    this.frame = new Frame(TITLE);
    frame.add(glCanvas);
    frame.setVisible(true);
    frame.addWindowListener(new WindowAdapter() {
      @Override public void windowClosing(WindowEvent e) { System.exit(0); }
    });

    this.imageCopy = new GLBufferedImageCanvas(20, 20);

    requestFocus();
  }

  @Override
  public void init(GLAutoDrawable glDrawable) {
    super.init(glDrawable);
    setCameraDistance(15);
    retrieveCamera();
  }
  
  public void requestFocus() {
    frame.toFront();
    glCanvas.requestFocus();
  }

  public void setImageSize(int width, int height) {
    width = width + (width % 4);
    height = height + (height % 4);
    frame.setSize(width + WINDOW_PADDING_X, height + WINDOW_PADDING_Y);
    imageCopy = new GLBufferedImageCanvas(width, height);
  }

  public Dimension getImageSize() {
    return new Dimension(imageCopy.getWidth(), imageCopy.getHeight());
  }

  public Dimension getFrameSize() {
    return frame.getSize();
  }

  public void update() {
    glCanvas.display();
  }

  public GLBufferedImageCanvas getCanvasCopy() {
    return imageCopy;
  }

  @Override
  protected void draw() {
    backbone.getScene().render(this);
    imageCopy.copyContextToCanvas(gl);
    updateFrameTitle();
  }

  @Override
  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
    case KeyEvent.VK_OPEN_BRACKET:
      saveCamera();
      backbone.previousScene();
      retrieveCamera();
      break;
    case KeyEvent.VK_CLOSE_BRACKET:
      saveCamera();
      backbone.nextScene();
      retrieveCamera();
      break;
    case KeyEvent.VK_P:
      String screenshot = FileSystemView.getFileSystemView().getHomeDirectory()
          + "\\" + backbone.getScene().getName() + ".png";
      if (e.isControlDown()) {
        backbone.getRayTracer().getCanvas().write(screenshot);
      } else {
        imageCopy.write(screenshot);
      }
      break;
    case KeyEvent.VK_R:
      if (e.isControlDown()) {
        backbone.getRayTracer().getRayTracer().setSupersamplingMultiple(1);
      } else {
        backbone.getRayTracer().getRayTracer().setSupersamplingMultiple(
            backbone.getScene().getSupersamplingMultiple());
      }
      backbone.rayTrace();
      break;
    case KeyEvent.VK_A:
      backbone.animate();
      break;
    default:
      super.keyPressed(e);
      break;
    }
  }
  
  private void saveCamera() {
    Preferences prefs = Preferences.userNodeForPackage(MovieMakerEditor.class);
    prefs.put("CameraMatrix:" + backbone.getScene().getName(), 
        getCamera().getLocalToParent().toString());
  }
  
  private void retrieveCamera() {
    Preferences prefs = Preferences.userNodeForPackage(MovieMakerEditor.class);    
    String cameraPos = prefs.get("CameraMatrix:" + backbone.getScene().getName(), null);
    if (cameraPos != null) {
      getCamera().setLocalToParent(M4x4.fromString(cameraPos));
    }
  }

  private void updateFrameTitle() {
    String title = TITLE + " - " + backbone.getScene().getName();
    if (isAnimating()) {
      int tick = backbone.getAnimator().getTick();
      int maxTick = backbone.getAnimator().getMaxTick();
      long soFar = System.currentTimeMillis() - backbone.getAnimator().getStartedAt();
      long etd = (tick >= 10) ? (soFar * maxTick / tick) - soFar : 0;
      Date eta = new Date(System.currentTimeMillis() + etd);
      frame.setTitle(title + ": [" + (tick + 1) + "/" + maxTick
          + ((etd > 0) ? ", ETA " + DateFormat.getTimeInstance().format(eta) : "") + "]");
    } else if (!frame.getTitle().equals(title)) {
      frame.setTitle(title);
    }
  }
  
  private boolean isAnimating() {
    return (backbone.getAnimator().getTick() != backbone.getAnimator().getMaxTick());
  }
}
