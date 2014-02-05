package com.bentonian.framework.io;


import static com.bentonian.framework.io.FileUtil.readFile;

import java.io.InputStream;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.glu.GLU;

public class ShaderUtil {
  
  private static final int BUFFER_SIZE = 4096;

  public static int[] getStringLengths(String[] strings) {
    int[] lengths = new int[strings.length];
    for (int i = 0; i < strings.length; i++) {
      lengths[i] = strings[i].length();
    }
    return lengths;
  }

  public static int compileProgram(GL4 gl, int vShader, int fShader) {
    int shaderProgram = gl.glCreateProgram();
    
    gl.glAttachShader(shaderProgram, vShader);
    testGlError(gl);
    gl.glAttachShader(shaderProgram, fShader);
    testGlError(gl);
    gl.glLinkProgram(shaderProgram);
    testGlError(gl);

    gl.glValidateProgram(shaderProgram);
    checkProgram(gl, shaderProgram);

    return shaderProgram;
  }

  public static int loadShader(GL4 gl, int shaderType, String file) {
    int shader = gl.glCreateShader(shaderType);
    String[] lines = readFile(file).toArray(new String[]{});
    gl.glShaderSource(shader, lines.length, lines, getStringLengths(lines), 0 /* offset */);
    testGlError(gl);
    gl.glCompileShader(shader);
    checkShader(gl, shader, file);
    return shader;
  }

  public static int loadShader(GL4 gl, int shaderType, Class<?> clazz, String resourceName) {
    int shader = gl.glCreateShader(shaderType);
    InputStream resource = clazz.getResourceAsStream(resourceName);
    String[] lines = FileUtil.readResource(resource).toArray(new String[]{});
    gl.glShaderSource(shader, lines.length, lines, ShaderUtil.getStringLengths(lines), 0 /* offset */);
    testGlError(gl);
    gl.glCompileShader(shader);
    checkShader(gl, shader, resourceName);
    return shader;
  }

  public static void checkShader(GL4 gl, int shader, String description) {
    testGlError(gl);
    int[] length = new int[1];
    byte[] buffer = new byte[BUFFER_SIZE];
    gl.glGetShaderInfoLog(shader, BUFFER_SIZE, length, 0, buffer, 0);
    int logLength = length[0];
    if (logLength > 1) {
      String infolog = new String(buffer);
      if (infolog.trim().compareTo("No errors.") != 0) {
        checkVersion(gl);
        System.out.println("Info log for shader '" + description + "' (ID " + shader + "):");
        System.out.println(infolog);
        new RuntimeException().printStackTrace();
        System.exit(-1);
      }
    }
  }

  public static void checkVersion(GL4 gl) {
    System.out.println("GL version: " + gl.glGetString(GL.GL_VERSION));
  }

  public static void checkProgram(GL4 gl, int program) {
    testGlError(gl);
    int[] length = new int[1];
    byte[] buffer = new byte[BUFFER_SIZE];
    gl.glGetProgramInfoLog(program, BUFFER_SIZE, length, 0, buffer, 0);
    int logLength = length[0];
    String infolog = new String(buffer);
    if (logLength > 1) {
      infolog = infolog.trim();
      if (!infolog.isEmpty() && !infolog.equalsIgnoreCase("No errors.")) {
        checkVersion(gl);
        System.out.println("Info Log of Program Object ID: " + program);
        System.out.println(infolog);
        new RuntimeException().printStackTrace();
        System.exit(-1);
      }
    }
  }
  
  public static void testGlError(GL4 gl) {
    if (gl != null) {
      int error = gl.glGetError();
      if (error != 0) {
        RuntimeException e = new RuntimeException(
            "OpenGL Error " + error + ": " + (new GLU()).gluErrorString(error));
        e.printStackTrace();
        System.exit(-1);
      }
    }
  }

  public static int validateLocation(int location, String name) {
    if (location < 0) {
      RuntimeException e = new RuntimeException(
          "Whoops!  Couldn't find " + name + ".");
      e.printStackTrace();
      System.exit(-1);
    }
    return location;
  }
}
