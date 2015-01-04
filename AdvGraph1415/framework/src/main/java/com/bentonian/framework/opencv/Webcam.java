package com.bentonian.framework.opencv;

import java.util.Set;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class Webcam  {

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  private static final Set<Integer> OPEN_WEBCAMS = Sets.newHashSet();

  private final int webcamIndex;
  private final VideoCapture vc;
  private final CvMatAccessor matAccess;

  private boolean webcamOn;
  private boolean shutdownComplete;
  protected boolean newFrameSought;
  protected boolean newFrameAvailable;

  public Webcam(int webcamIndex) {
    Preconditions.checkState(webcamIndex >= 0);
    Preconditions.checkState(!OPEN_WEBCAMS.contains(webcamIndex));

    this.vc = new VideoCapture();
    this.matAccess = new CvMatAccessor(open(webcamIndex));
    this.webcamOn = true;
    this.webcamIndex = webcamIndex;
    this.newFrameSought = true;
    this.shutdownComplete = false;
    this.newFrameAvailable = false;

    OPEN_WEBCAMS.add(webcamIndex);
    startBackgroundThread();
  }

  public int getWebcamIndex() {
    return webcamIndex;
  }

  public int getRows() {
    return matAccess.getRows();
  }

  public int getCols() {
    return matAccess.getCols();
  }
  
  public boolean isNewFrameAvailable() {
    return newFrameAvailable;
  }

  public synchronized void copyTo(Mat m) {
    if (newFrameAvailable) {
      matAccess.getMat().copyTo(m);
      newFrameAvailable = false;
      newFrameSought = true;
    }
  }

  public void bind(int textureId) {
    if (newFrameAvailable) {
      matAccess.bindToOpenGlTexture(textureId);
      newFrameAvailable = false;
      newFrameSought = true;
    }
  }

  public void dispose() {
    if (webcamOn) {
      webcamOn = false;
      while (!shutdownComplete) {
        snooze(10);
      }
      OPEN_WEBCAMS.remove(webcamIndex);
    }
  }

  private Mat open(int webcamIndex) {
    Mat mat = new Mat();

    if (!vc.open(webcamIndex)) {
      throw new RuntimeException("Failed to open video capture for source " + webcamIndex);
    }

    int attempts = 0;
    while ((mat.cols() == 0 || mat.rows() == 0) && attempts++ < 10) {
      snooze(30);
      vc.read(mat);
    }
    if  (mat.cols() == 0 || mat.rows() == 0) {
      throw new RuntimeException("Dude, where's my webcam?");
    }
    return mat;
  }

  private void startBackgroundThread() {
    new Thread() {
      @Override
      public void run() {
        while (webcamOn) {
          if (newFrameSought) {
            updateFromWebcam();
          }
          snooze(33);
        }
        vc.release();
        shutdownComplete = true;
      }
    }.start();
  }

  private void snooze(int time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) { }
  }

  /**
   * Converts a Mat into a ByteBuffer padded out to RGBA.
   */
  private synchronized void updateFromWebcam() {
    if (vc.isOpened()) {
      vc.read(matAccess.getMat());
      matAccess.extractDataFromMat(Imgproc.COLOR_BGR2RGB);
      newFrameSought = false;
      newFrameAvailable = true;
    }
  }
}
