package com.bentonian.helloshader;

import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.google.common.base.Joiner;
import com.google.common.primitives.Floats;
import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.PMVMatrix;


/**
 * Sample code to demonstrate the basics of OpenGL.
 * Written for delivery to Advanced Graphics, Cambridge University, 2013-2014.
 * 
 * This class draws a wireframe cube in perspective.  It uses GL_LINE_STRIP
 * and a vertex buffer.  It registers a very simple vertex shader whose sole
 * function is to apply a transform to input vertices, and a very simple
 * fragment shader whose output is always white.
 *
 * This class relies on your having correctly configured JOGL to run.
 * Download the latest JOGL by finding the latest versions of GLUEGEN and JOGL at
 *   http://jogamp.org/deployment/autobuilds/master/
 * For reference, this code was written against 
 *   gluegen-2.1-b757-20131219-windows-amd64
 *   jogl-2.1-b1177-20131219-windows-amd64
 *   
 * This class uses Guava, the Google Java extension library.  Download Guava at
 *   https://code.google.com/p/guava-libraries/
 *
 * @author Alex Benton
 */
public class HelloShader2 implements GLEventListener {

  static final float[][] CUBE_CORNERS = {
    {-0.8f, 0.8f, 0.8f}, { 0.8f, 0.8f, 0.8f}, { 0.8f, 0.8f,-0.8f}, {-0.8f, 0.8f,-0.8f},
    {-0.8f,-0.8f, 0.8f}, { 0.8f,-0.8f, 0.8f}, { 0.8f,-0.8f,-0.8f}, {-0.8f,-0.8f,-0.8f},
  };
  static final int[] INDICES = { 0, 1, 2, 3, 0, 4, 5, 1, 5, 6, 2, 6, 7, 3, 7, 4 };
  
  private static final String vertexShader = Joiner.on("\n").join(
    "#version 330",
    "",
    "uniform mat4 modelToScreen;",
    "",
    "in vec4 vPosition;",
    "",
    "void main() {",
    "  gl_Position = modelToScreen * vPosition;",
    "}"
  );

  private static final String fragmentShader = Joiner.on("\n").join(
    "#version 330",
    "",
    "out vec4 c;",
    "",
    "void main() {",
    "  c = vec4(1, 1, 1, 1);",
    "}"
  );

  private final int[] vertexBufferId = new int[1];

  @Override
  public void init(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);

    int program = createAndInstallProgram(gl);
    applyPerspectiveTransform(gl, program);
    fillVertexBuffer(gl);
  }

  private int createAndInstallProgram(GL4 gl) {
    int program = gl.glCreateProgram();

    int vsName = gl.glCreateShader(GL4.GL_VERTEX_SHADER);
    gl.glShaderSource(vsName, 1, new String[]{vertexShader}, new int[]{vertexShader.length()}, 0);
    gl.glCompileShader(vsName);
    gl.glAttachShader(program, vsName);

    int fsName = gl.glCreateShader(GL4.GL_FRAGMENT_SHADER);
    gl.glShaderSource(fsName, 1, new String[]{fragmentShader}, new int[]{fragmentShader.length()}, 0);
    gl.glCompileShader(fsName);
    gl.glAttachShader(program, fsName);

    gl.glLinkProgram(program);
    gl.glValidateProgram(program);
    gl.glUseProgram(program);

    return program;
  }

  private void applyPerspectiveTransform(GL4 gl, int program) {
    PMVMatrix T = new PMVMatrix();
    T.glMatrixMode(PMVMatrix.GL_PROJECTION);
    T.glFrustumf(-0.005f, 0.005f, -0.005f, 0.005f, 0.01f, 10);
    float[] perspective = Arrays.copyOfRange(T.glGetPMvMatrixf().array(), 32, 48);
    int location = gl.glGetUniformLocation(program, "modelToScreen");
    gl.glUniformMatrix4fv(location, 1, false, perspective, 0);
  }

  private void fillVertexBuffer(GL4 gl) {

    // Create and bind OpenGL vertex buffer object
    gl.glGenBuffers(1, vertexBufferId, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);

    // Fill Java FloatBuffer
    FloatBuffer vertices = Buffers.newDirectFloatBuffer(INDICES.length * 3);
    for (int index : INDICES) {
      vertices.put(CUBE_CORNERS[index]);
    }
    vertices.rewind();
    
    // Translate all vertices -3 along the Z axis
    for (int i = 0; i < INDICES.length; i++) {
      vertices.put(i*3 + 2, vertices.get(i*3 + 2) - 3);
    }

    // Pass Java FloatBuffer data to OpenGL vertex buffer object
    gl.glBufferData(GL.GL_ARRAY_BUFFER, Floats.BYTES * INDICES.length * 3, vertices, GL.GL_STATIC_DRAW);

    // Tell OpenGL to pass the current vertex buffer object to parameter 0 in the shader
    gl.glEnableVertexAttribArray(0);
    gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);
  }

  @Override
  public void display(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glDrawArrays(GL.GL_LINE_STRIP, 0, INDICES.length);
  }

  @Override
  public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
  }

  @Override
  public void dispose(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    gl.glDeleteBuffers(1, vertexBufferId, 0);
  }

  public static void main(String[] args) {
    HelloShaderAWTFrame.wrap("Hello Cube", new HelloShader2());
  }
}
