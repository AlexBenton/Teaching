package com.bentonian.hellosquare;

import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.jogamp.common.nio.Buffers;

public class HelloSquareRenderer implements GLEventListener {

  @Override
  public void init(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);

    createAndBindVertexBuffer(gl);
    fillCurrentVertexBuffer(gl);
    
    gl.glEnableVertexAttribArray(0);
    gl.glVertexAttribPointer(0, 2, GL.GL_FLOAT, false, 0, 0);
  }
  
  private void createAndBindVertexBuffer(GL4 gl) {
    int[] arrays = {0};
    gl.glGenBuffers(1, arrays, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, arrays[0]);
  }
  
  private void fillCurrentVertexBuffer(GL4 gl) {
    FloatBuffer vertices = Buffers.newDirectFloatBuffer(new float[] {
        -0.6f,  0.5f,     0.4f,  0.5f,      -0.6f, -0.5f,     // First triangle
         0.6f, -0.5f,     0.6f,  0.5f,      -0.4f, -0.5f,     // Second triangle
      });
      gl.glBufferData(GL.GL_ARRAY_BUFFER, 
          Float.SIZE * 2 * 2 * 3, vertices, GL.GL_STATIC_DRAW);
  }

  @Override
  public void display(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT);
    gl.glDrawArrays(GL.GL_TRIANGLES, 0, 2 * 2 * 3);
  }

  @Override
  public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
  }

  @Override
  public void dispose(GLAutoDrawable glDrawable) {
  }
}
