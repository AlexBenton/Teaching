package com.bentonian.jogldemos.moviemaker;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

import com.bentonian.framework.io.AVIEncoder;
import com.bentonian.framework.io.AnimatedGifEncoder;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class MovieMakerAnimator {
  
  private static final int GIF_TOO_BIG = 5 * 1024 * 1024;

  public interface MovieElement {
    public int getStartTick();
    public int getEndTick();
    public void start();
    public void onTick(double t);
    public void done();
  }
  
  public static abstract class StockMovieElement implements MovieElement {
    @Override public int getStartTick() { return 0; }
    @Override public int getEndTick() { return 201; }
    @Override public void start() { }
    @Override public void done() { }
  }

  private final List<MovieElement> elements;
  private final MovieMakerBackbone backbone;

  private String filename;
  private long startedAt;
  private int tick;
  private int maxTick;
  private boolean autoReverse;
  private List<BufferedImage> frames;
  private boolean record;
  private boolean loop;
  private boolean captureRayTracer;

  public MovieMakerAnimator(MovieMakerBackbone backbone) {
    this.backbone = backbone;
    this.elements = Lists.newLinkedList();
    this.filename = "";
    this.startedAt = 0;
    this.loop = true;
    this.record = true;
    this.captureRayTracer = true;
  }

  public void setAnimationName(String name) {
    this.filename = FileSystemView.getFileSystemView().getHomeDirectory() + "\\" + name;
  }

  public void setAutoReverse(boolean autoReverse) {
    this.autoReverse = autoReverse;
  }

  public void setRecording(boolean record) {
    this.record = record;
  }

  public void setLoop(boolean loop) {
    this.loop = loop;
  }

  public void setCaptureRayTracer(boolean captureRayTracer) {
    this.captureRayTracer = captureRayTracer;
  }

  public int getTick() {
    return tick;
  }

  public int getMaxTick() {
    return maxTick;
  }

  public long getStartedAt() {
    return startedAt;
  }

  public void addMovieElement(MovieElement element) {
    elements.add(element);
  }

  public void clearMovieElements() {
    elements.clear();
  }

  public void animate() {
    if (record) {
      frames = Lists.newArrayList();
    }

    if (!elements.isEmpty()) {
      for (MovieElement element : elements) {
        element.start();
      }
    }
    this.tick = 0;
    this.maxTick = findMaxTick();
  }

  public void stop() {
    if (tick < maxTick) {
      this.tick = maxTick;
      if (record) {
        System.out.println("Recording canceled.");
      }
    }
  }

  public void preDisplay() {
    if (tick < maxTick) {
      if (record) {
        System.out.println("Beginning tick " + (tick + 1) + " of " + maxTick);
      }
      if (tick == 0) {
        startedAt = System.currentTimeMillis();
      }
      for (MovieElement element : elements) {
        if ((tick >= element.getStartTick()) && (tick < element.getEndTick())) {
          double s = tick - element.getStartTick();
          double m = (element.getEndTick() - 1) - element.getStartTick();
          element.onTick(s / m);
        }
      }
      if (captureRayTracer) {
        backbone.rayTrace();
      }
    }
  }

  public void postDisplay() {
    if ((tick < maxTick) && (startedAt != 0)) {
      BufferedImage frame = captureRayTracer
          ? backbone.getRayTracer().getCanvas()
          : backbone.getEditor().getCanvasCopy();

      tick++;
      if (record) {
        System.out.println("End of tick " + tick);
        frames.add(deepCopy(frame));
      }

      for (MovieElement element : elements) {
        if (tick == element.getEndTick()) {
          element.done();
        }
      }

      if (tick == maxTick) {
        if (record) {
          long then = System.currentTimeMillis();
          System.out.println("Building animations...");

          if (autoReverse) {
            System.out.println("Reversing frames...");
            for (int i = frames.size() - 1; i >= 0; i--) {
              frames.add(deepCopy(frames.get(i)));
            }
          }

          writeAvi(frames, filename);
          writeGif(frames, filename);
          if (gifsize(filename) > GIF_TOO_BIG) {
            shrinkGif(frames, filename + " (redux)");
          }
          System.out.println("Recordings finalized in "
              + (System.currentTimeMillis() - then) / 1000 + "s.");
        }

        System.out.println("Animation complete.");
        startedAt = 0;

        if (loop) {
          animate();
        }
      }
    }
  }
  
  private void shrinkGif(List<BufferedImage> frames, String filename) {
    double scale = 1;
    int width = frames.get(0).getWidth();
    int height = frames.get(0).getHeight();
    boolean notSmallEnoughYet = true;
    long filesize = 0;
    
    System.out.println("Shrinking GIF...");
    while (notSmallEnoughYet && (scale > 0.1)) {
      scale -= 0.05;
      writeGif(
          FluentIterable.from(frames).transform(scale(width, height, scale)).toList(), filename);
      filesize = gifsize(filename);
      notSmallEnoughYet = (filesize > GIF_TOO_BIG);
    }
    if (!notSmallEnoughYet) {
      System.out.println("Success!  Scale = " + (100 * scale) + "%, file size = " + filesize);
    } else {
      System.out.println("Curses!  Couldn't shrink the GIF below 5MB.");
    }
  }
  
  private Function<BufferedImage, BufferedImage> scale(
      int sourceWidth, int sourceHeight, double scale) {
    AffineTransform at = new AffineTransform();
    at.scale(scale, scale);
    final AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
    final int w = (int) (sourceWidth * scale);
    final int h = (int) (sourceHeight * scale);
    
    System.out.println("Writing " + w + " x " + h + " (scale: " + (100 * scale) + "%)");
    return new Function<BufferedImage, BufferedImage>() {
      @Override
      public BufferedImage apply(BufferedImage input) {
        return scaleOp.filter(input, new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB));
      }
    };
  }

  private long gifsize(String filename) {
    return new File(filename + ".gif").length();
  }

  private void writeGif(List<BufferedImage> frames, String filename) {
    System.out.println("Building GIF...");

    AnimatedGifEncoder gifEncoder = new AnimatedGifEncoder();
    gifEncoder.setFrameRate(33);
    gifEncoder.setQuality(1);
    gifEncoder.setRepeat(0);
    gifEncoder.start(filename + ".gif");
    for (BufferedImage source : frames) {
      gifEncoder.addFrame(source);
    }
    gifEncoder.finish();

    System.out.println("...GIF complete.");
  }

  private void writeAvi(List<BufferedImage> frames, String filename) {
    System.out.println("Writing AVI...");
    AVIEncoder aviEncoder = new AVIEncoder();
    try {
      aviEncoder.write(filename + ".avi", frames);
      System.out.println("...AVI complete.");
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("...failed to write AVI.");
    }
  }

  private BufferedImage deepCopy(BufferedImage bi) {
    ColorModel cm = bi.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = bi.copyData(null);
    BufferedImage copy = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    Preconditions.checkState(
        (bi.getWidth() == copy.getWidth()) && (bi.getHeight() == copy.getHeight()));
    return copy;
   }

  private int findMaxTick() {
    int maxTick = Iterables.get(elements, 0).getEndTick();
    for (MovieElement element : elements) {
      if (maxTick < element.getEndTick()) {
        maxTick = element.getEndTick();
      }
    }
    return maxTick;
  }
}
