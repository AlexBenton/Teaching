package com.bentonian.gldemos.raytracedtexture;

import static com.bentonian.framework.io.FileUtil.writeImageToPng;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;

import com.bentonian.framework.io.FileUtil;
import com.bentonian.framework.texture.TexCoord;

public class SignedDistanceField {

  private final double[][] sdf;
  private final String cacheFileName;

  public SignedDistanceField(BufferedImage image) {
    this.cacheFileName = "sdf-cache-" + image.hashCode() + ".png";
    
    // Try to use cached heightmap
    BufferedImage heightMap = (new File(cacheFileName)).exists() 
        ? FileUtil.loadImageFromFile(cacheFileName) : null;

    this.sdf = new double[image.getWidth()][image.getHeight()];
    if (heightMap != null 
        && heightMap.getWidth() == image.getWidth() 
        && heightMap.getHeight() == image.getHeight()) {

      // Retrieve precomputed distance map from heightmap
      System.out.println("Using cached SDF");
      for (int i = 0; i < image.getWidth(); i++) {
        for (int j = 0; j < image.getHeight(); j++) {
          sdf[i][j] = ((heightMap.getRGB(i, j) & 0xFF) - 128) / 255.0;
        }
      }
    } else {

      // Compute distance map
      double furthest = 0;
      boolean[][] bw = new boolean[image.getWidth()][image.getHeight()];

      System.out.println("Building SDF...");
      for (int i = 0; i < image.getWidth(); i++) {
        for (int j = 0; j < image.getHeight(); j++) {
          bw[i][j] = isWhite(image, i, j);
        }
      }
      for (int i = 0; i < image.getWidth(); i++) {
        System.out.println("  " + new DecimalFormat("#.00").format(100.0 * i / (image.getWidth() - 1)) + "%");
        for (int j = 0; j < image.getHeight(); j++) {
          boolean isWhite = isWhite(image, i, j);
          double d = findNearest(bw, i, j, isWhite);
          furthest = max(d, furthest);
          sdf[i][j] = (isWhite ? 1 : -1) * d;
        }
      }
      for (int i = 0; i < image.getWidth(); i++) {
        for (int j = 0; j < image.getHeight(); j++) {
          sdf[i][j] /= furthest;
        }
      }

      // Save distance map as heightmap image
      heightMap = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
      for (int i = 0; i < image.getWidth(); i++) {
        for (int j = 0; j < image.getHeight(); j++) {
          int n = ((int) ((sdf[i][j] + 1) * 255 / 2)) & 0xFF;
          heightMap.setRGB(i, j, n | (n<<8) | (n<<16) | (0xFF<<24));
        }
      }
      writeImageToPng(heightMap, cacheFileName);
    }
  }

  private double findNearest(boolean[][] bw, int i, int j, boolean isWhite) {
    double dist = Double.MAX_VALUE;
    for (int x = 0; x < sdf.length; x++) {
      for (int y = 0; y < sdf[0].length; y++) {
        if (isWhite != bw[x][y]) {
          double d = (x-i)*(x-i)+(y-j)*(y-j);
          if (d < dist) {
            dist = d;
          }
        }
      }
    }
    return sqrt(dist);
  }

  public static boolean isWhite(BufferedImage image, int i, int j) {
    int argb = image.getRGB(i, j);
    int r = (argb >> 16) & 0xFF;
    int g = (argb >> 8) & 0xFF;
    int b = (argb >> 0) & 0xFF;
    return (r >= 128 || g >= 128 || b >= 128);
  }

  public double sample(TexCoord tc) {
    double x = (sdf.length - 1) * tc.u;
    double y = (sdf[0].length - 1) * tc.v;
    double gap_x = x - floor(x);
    double gap_y = y - floor(y);
    int off_x = (gap_x < 0.5) ? 0 : 1;
    int off_y = (gap_y < 0.5) ? 0 : 1;

    return linterp(
        linterp(
            get(x - 1 + off_x, y - 1 + off_y),
            get(x + off_x, y - 1 + off_y),
            gap_x + 0.5 - off_x),
        linterp(
            get(x - 1 + off_x, y + off_y),
            get(x + off_x, y + off_y),
            gap_x + 0.5 - off_x),
        gap_y + 0.5 - off_y);
  }

  private double linterp(double a, double b, double t) {
    return a * (1 - t) + b * t;
  }

  private double get(double x, double y) {
    int i = min(sdf.length - 1, max(0, (int) x));
    int j = min(sdf[0].length - 1, max(0, (int) y));
    return sdf[i][j];
  }
}