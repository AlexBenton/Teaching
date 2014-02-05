package com.bentonian.helloshader;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.jogamp.common.nio.Buffers;


/**
 * Sample code to demonstrate the basics of OpenGL.
 * Written for delivery to Advanced Graphics, Cambridge University, 2013-2014.
 * 
 * This class demonstrates drawing two triangles to the screen in clip coordinates.
 * It uses demonstrates the use of GL_TRIANGLES and a vertex buffer.  It does not 
 * register any shaders and relies on the default JOGL shader behavior.
 * 
 * This class relies on your having correctly configured JOGL to run.
 * Download the latest JOGL by finding the latest versions of GLUEGEN and JOGL at
 *   http://jogamp.org/deployment/autobuilds/master/
 * For reference, this code was written against 
 *   gluegen-2.1-b757-20131219-windows-amd64
 *   jogl-2.1-b1177-20131219-windows-amd64
 *
 * @author Alex Benton
 */
public class HelloShader1 implements GLEventListener {

  private static final float[] VERTICES = new float[] {
    -0.6f,  0.5f,     0.4f,  0.5f,      -0.6f, -0.5f,     // First triangle
     0.6f, -0.5f,     0.6f,  0.5f,      -0.4f, -0.5f,     // Second triangle
  };

  private final int[] vertexBufferId = new int[1];

  @Override
  public void init(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);

    fillVertexBuffer(gl);
  }

  private void fillVertexBuffer(GL4 gl) {

    // Create and bind OpenGL vertex buffer object
    gl.glGenBuffers(1, vertexBufferId, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);

    // Fill Java FloatBuffer
    FloatBuffer buffer = Buffers.newDirectFloatBuffer(VERTICES.length);
    buffer.put(VERTICES);
    buffer.rewind();

    // Pass Java FloatBuffer data to OpenGL vertex buffer object
    gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.SIZE * VERTICES.length, buffer, GL.GL_STATIC_DRAW);

    // Tell OpenGL to pass the current vertex buffer object to parameter 0 in the shader
    gl.glEnableVertexAttribArray(0);
    gl.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 0, 0);
  }

  @Override
  public void display(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glDrawArrays(GL.GL_TRIANGLES, 0, VERTICES.length);
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
    HelloShaderAWTFrame.wrap("Hello Square", new HelloShader1());
  }
}
