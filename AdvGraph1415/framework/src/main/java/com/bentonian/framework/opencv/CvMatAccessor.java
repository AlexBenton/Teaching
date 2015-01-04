package com.bentonian.framework.opencv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import com.bentonian.framework.ui.GLCanvas;

public class CvMatAccessor {

  private final Mat mat;
  private final int rows, cols;
  private final byte[] readBuffer;
  private final ByteBuffer byteBuffer;

  public CvMatAccessor(Mat mat) {
    this.mat = mat;
    this.rows = mat.rows();
    this.cols = mat.cols();
    this.byteBuffer = ByteBuffer.allocateDirect(cols * rows * 3);
    this.readBuffer = new byte[cols * rows * 3];
  }

  public Mat getMat() {
    return mat;
  }

  public int getRows() {
    return rows;
  }

  public int getCols() {
    return cols;
  }
  
  public void extractDataFromMat(int openCvConversion) {
    if (openCvConversion != -1) {
      Imgproc.cvtColor(mat, mat, openCvConversion);
    }
    mat.get(0, 0, readBuffer);
    byteBuffer.put(readBuffer);
    byteBuffer.flip();
  }

  public void bindToOpenGlTexture(int textureId) {
    GLCanvas.updateTextureBuffer(textureId, byteBuffer, cols, rows, GL11.GL_RGB);
  }

  public static BufferedImage toBufferedImage(Mat mat){
    int type = BufferedImage.TYPE_BYTE_GRAY;
    if (mat.channels() > 1) {
      type = BufferedImage.TYPE_3BYTE_BGR;
    }
    int bufferSize = mat.channels() * mat.cols() * mat.rows();
    byte [] b = new byte[bufferSize];
    mat.get(0,0,b); // get all the pixels
    BufferedImage image = new BufferedImage(mat.cols(),mat.rows(), type);
    final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(b, 0, targetPixels, 0, b.length);
    return image;
  }
}
