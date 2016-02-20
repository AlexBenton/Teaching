package com.bentonian.framework.ui;

import static com.bentonian.framework.io.FileUtil.readFile;
import static com.bentonian.framework.io.FileUtil.readResource;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.OpenGLException;
import org.lwjgl.opengl.Util;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;


public class ShaderUtil {

  private static final int BUFFER_SIZE = 4096;
  private static final Reader FILE_READER = new Reader() {
    @Override
    public List<String> read(String name) {
      return readFile(name);
    }
  };

  public static int compileProgram(int vShader, int fShader) {
    int shaderProgram = glCreateProgram();

    glAttachShader(shaderProgram, vShader);
    testGlError();
    glAttachShader(shaderProgram, fShader);
    testGlError();
    glLinkProgram(shaderProgram);
    testGlError();

    glValidateProgram(shaderProgram);
    checkProgram(shaderProgram);

    return shaderProgram;
  }

  public static int loadShader(int shaderType, String file) {
    return loadShaderWithLines(shaderType, file, readGlslWithInclude(FILE_READER, file));
  }

  public static int loadShader(int shaderType, final Class<?> clazz, String resourceName) {
    return loadShaderWithLines(shaderType, resourceName, readGlslWithInclude(
        new Reader() {
          @Override
          public List<String> read(String name) {
            return readResource(clazz, name);
          }
        }, resourceName));
  }

  public static void checkProgram(int program) {
    testGlError();
    String infolog = glGetProgramInfoLog(program, BUFFER_SIZE);
    if (!Strings.isNullOrEmpty(infolog)
        && !infolog.trim().equals("No errors.")
        && !infolog.contains("WARNING:")) {
      infolog = infolog.trim();
      System.out.println("Info Log of Program Object ID: " + program);
      System.out.println(infolog);
      new RuntimeException().printStackTrace();
      System.exit(-1);
    }
    if (!Strings.isNullOrEmpty(infolog)) {
      System.out.println(infolog.trim());
    }
  }

  public static void testGlError() {
    try {
      Util.checkGLError();
    } catch (OpenGLException e) {
      e.setStackTrace(Arrays.copyOfRange(e.getStackTrace(), 2, e.getStackTrace().length));
      e.printStackTrace();
      System.exit(-1);
    }
  }

  public static void clearGlError() {
    try {
      Util.checkGLError();
    } catch (OpenGLException e) {
      System.out.println("Clearing OpenGL error " + e.getMessage());
    }
  }

  public static int validateLocation(int location, String name) {
    if (location < 0) {
      RuntimeException e = new RuntimeException("Whoops!  Couldn't find " + name + ".");
      e.printStackTrace();
      System.exit(-1);
    }
    return location;
  }

  public static void printGlVersion() {
    System.out.println("GL version: " + GL11.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION));
  }

  private static int loadShaderWithLines(int shaderType, String name, List<TrackedFileLine> lines) {
    int shader = loadShaderWithoutChecks(shaderType, name, 
        FluentIterable.from(lines).transform(TrackedFileLine.TO_STRING).toList());
    checkShaderWithLineErrors(shader, name, lines);
    return shader;
  }

  private static int loadShaderWithoutChecks(int shaderType, String name, List<String> lines) {
    int shader = glCreateShader(shaderType);
    glShaderSource(shader, lines.toArray(new String[]{}));
    testGlError();
    glCompileShader(shader);
    return shader;
  }

  private static class TrackedFileLine {
    
    static Function<TrackedFileLine, String> TO_STRING = new Function<TrackedFileLine, String>() {
      @Override
      public String apply(TrackedFileLine input) {
        return input.line;
      } 
    };

    String line;
    String lineSource;
    int lineNumber;

    public TrackedFileLine(String line, String lineSource, int lineNumber) {
      this.line = line;
      this.lineSource = lineSource;
      this.lineNumber = lineNumber;
    }
  }

  private interface Reader {
    public abstract List<String> read(String name);
  }

  private static List<TrackedFileLine> readGlslWithInclude(Reader reader, String resourceName) {
    List<String> lines = reader.read(resourceName);
    List<TrackedFileLine> trackedLines = Lists.newArrayList();
    int i = 0;

    for (String line : lines) {
      if (!line.trim().startsWith("#include \"")) {
        trackedLines.add(new TrackedFileLine(line, resourceName, i++));
      } else {
        trackedLines.addAll(readGlslWithInclude(reader, 
            line.trim().replace("#include ", "").replace("\"", "")));
      }
    }
    return trackedLines;
  }

  private static void checkShaderWithLineErrors(
      int shader, String description, List<TrackedFileLine> tracked) {
    testGlError();
    String infolog = getShaderErrors(shader);
    if (!Strings.isNullOrEmpty(infolog)) {
      System.out.println("Info log for shader '" + description + "' (ID " + shader + "):");
      for (String infoLine : infolog.split("\n")) {
        if (infoLine.startsWith("0(") && infoLine.indexOf(")") > infoLine.indexOf("(")) {
          int n = Integer.valueOf(infoLine.substring(infoLine.indexOf("(") + 1, infoLine.indexOf(")")));
          if (n >= 0 && n < tracked.size()) {
            infoLine = infoLine + "(" + tracked.get(n).lineSource + ":" + (tracked.get(n).lineNumber + 2) + ")";
          }
        }
        System.out.println(infoLine);
      }
      new RuntimeException().printStackTrace();
      System.exit(-1);
    }
  }

  private static String getShaderErrors(int shader) {
    testGlError();
    String infolog = glGetShaderInfoLog(shader, BUFFER_SIZE);
    if (!Strings.isNullOrEmpty(infolog)
        && !infolog.trim().equals("No errors.")
        && !infolog.contains("WARNING:")) {
      infolog = infolog.trim();
      return infolog;
    }
    return "";
  }
}
