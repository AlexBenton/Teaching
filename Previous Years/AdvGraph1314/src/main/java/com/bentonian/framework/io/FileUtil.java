package com.bentonian.framework.io;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class FileUtil {

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

  public static BufferedImage loadImageResource(InputStream resource) {
    Preconditions.checkNotNull(resource);
    try {
      return ImageIO.read(resource);
    } catch (IOException e) {
      e.printStackTrace();
      System.exit(-1);
      return null;
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
