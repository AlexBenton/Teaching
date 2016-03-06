package com.bentonian.framework.io;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class FileUtil {

  public static byte[] readFileBytes(String filename) throws IOException {
    Path path = Paths.get(filename);
    return Files.readAllBytes(path);
  }
  
  public static List<String> readFile(String filename) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      return readStreamAndClose(reader);
    } catch (FileNotFoundException notFound) {
      return null;
    } catch (IOException notFound) {
      notFound.printStackTrace();
      System.exit(-1);
      return null;
    }
  }

  public static List<String> readResource(InputStream resource) {
    Preconditions.checkNotNull(resource);
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
      return readStreamAndClose(reader);
    } catch (IOException notFound) {
      notFound.printStackTrace();
      System.exit(-1);
      return null;
    }
  }
  
  public static List<String> readResource(Class<?> clazz, String resourceName) {
    InputStream resource = clazz.getResourceAsStream(resourceName);
    return readResource(resource);
  }

  public static BufferedImage loadImageResource(InputStream resource) {
    Preconditions.checkNotNull(resource);
    try {
      return ImageIO.read(resource);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static BufferedImage loadImageFromFile(String path) {
    try {
      return ImageIO.read(new File(path));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static boolean writeImageToPng(BufferedImage image, String path) {
    try {
      ImageIO.write(image, "png", new File(path));
      File f = new File(path);
      System.out.println("Wrote image at '" + f.getAbsolutePath() + "'");
      return true;
    } catch (RuntimeException | IOException e) {
      e.printStackTrace();
      return false;
    }
  }
  
  public static boolean writeGif(List<BufferedImage> frames, String filename) {
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
    return true;
  }

  public static boolean writeAvi(List<BufferedImage> frames, String filename) {
    System.out.println("Building AVI...");
    AVIEncoder aviEncoder = new AVIEncoder();
    try {
      aviEncoder.write(filename + ".avi", frames);
      System.out.println("...AVI complete.");
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      System.out.println("...failed to write AVI.");
      return false;
    }
  }
  
  public static long getFileTimestampHash(String path) {
    try {
      return new File(path).lastModified();
    } catch (Exception e) {
      return -1;
    }
  }

  public static long getDirectoryTimestampHash(String path) {
    try {
      long val = 0;
      for (File f : new File(path).listFiles()) {
        val ^= f.lastModified();
      }
      return val;
    } catch (Exception e) {
      return -1;
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private static List<String> readStreamAndClose(BufferedReader reader) throws IOException {
    List<String> lines = Lists.newArrayList();
    try {
      String line;
      while ((line = reader.readLine()) != null) {
        lines.add(line + "\n");
      }
      return lines;
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
      }
    }
  }
}
