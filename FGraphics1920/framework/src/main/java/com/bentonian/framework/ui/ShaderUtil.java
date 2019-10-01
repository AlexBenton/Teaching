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

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;


public class ShaderUtil {

  private static final int BUFFER_SIZE = 4096;

  public static int compileProgram(int vShader, int fShader) {
    int shaderProgram = glCreateProgram();

    glAttachShader(shaderProgram, vShader);
    glAttachShader(shaderProgram, fShader);
    glLinkProgram(shaderProgram);

    glValidateProgram(shaderProgram);
    checkProgram(shaderProgram);

    return shaderProgram;
  }

  public static int loadShader(int shaderType, String glslCode, String root) {
    return loadShaderWithLines(shaderType, "<str>", 
        readGlslWithInclude(
            (name) -> name.equals("<str>") ? Arrays.asList(glslCode.split("\n")) : readFile(name), 
            "<str>", root));
  }

  public static int loadShader(int shaderType, String file) {
    return loadShaderWithLines(shaderType, file, 
        readGlslWithInclude((name) -> readFile(name), file, file.substring(0, file.lastIndexOf('/') + 1)));
  }

  public static int loadShader(int shaderType, final Class<?> clazz, String resourceName) {
    return loadShaderWithLines(shaderType, resourceName, 
        readGlslWithInclude((name) -> readResource(clazz, name), resourceName, "include/"));
  }

  public static void checkProgram(int program) {
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

  public static String getShaderErrors(int shader) {
    String infolog = glGetShaderInfoLog(shader, BUFFER_SIZE);
    if (!Strings.isNullOrEmpty(infolog)
        && !infolog.trim().equals("No errors.")
        && !infolog.contains("WARNING:")) {
      infolog = infolog.trim();
      return infolog;
    }
    return "";
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

  public static int loadShaderWithLines(int shaderType, String name, List<TrackedFileLine> lines) {
    int shader = loadShaderWithoutChecks(shaderType, name, 
        FluentIterable.from(lines).transform(TrackedFileLine.TO_STRING).toList());
    checkShaderWithLineErrors(shader, name, lines);
    return shader;
  }

  private static int loadShaderWithoutChecks(int shaderType, String name, List<String> lines) {
    int shader = glCreateShader(shaderType);
    glShaderSource(shader, lines.toArray(new String[]{}));
    glCompileShader(shader);
    return shader;
  }

  private static class TrackedFileLine {
    
    static Function<TrackedFileLine, String> TO_STRING = (input) -> input.line;

    String line;
    String lineSource;
    int lineNumber;

    public TrackedFileLine(String line, String lineSource, int lineNumber) {
      this.line = line.endsWith("\n") ? line : (line + "\n");
      this.lineSource = lineSource;
      this.lineNumber = lineNumber;
    }
  }

  private static List<TrackedFileLine> readGlslWithInclude(
      Function<String, List<String>> reader, String resourceName, String root) {
    List<String> lines = reader.apply(resourceName);
    List<TrackedFileLine> trackedLines = Lists.newArrayList();
    int i = 0;

    for (String line : lines) {
      if (line.trim().startsWith("#include \"")) {
        String path = root + line.trim().replace("#include ", "").replace("\"", "");
        trackedLines.addAll(readGlslWithInclude(reader, path, root));
        i--;
      } else {
        trackedLines.add(new TrackedFileLine(line, resourceName, i++));
      }
    }
    return trackedLines;
  }

  private static void checkShaderWithLineErrors(
      int shader, String description, List<TrackedFileLine> tracked) {
    String infolog = getShaderErrors(shader);
    if (!Strings.isNullOrEmpty(infolog)) {
      StringBuilder builder = new StringBuilder();
      
      for (String infoLine : infolog.split("\n")) {
        if (infoLine.startsWith("0(") && infoLine.indexOf(")") > infoLine.indexOf("(")) {
          int n = Integer.valueOf(infoLine.substring(infoLine.indexOf("(") + 1, infoLine.indexOf(")")));
          if (n >= 0 && n < tracked.size()) {
            infoLine = infoLine + "(" + tracked.get(n).lineSource + ":" + (tracked.get(n).lineNumber + 2) + ")";
          }
        }
        builder.append(infoLine + "\n");
      }
      throw new RuntimeException(builder.toString());
    }
  }
}
