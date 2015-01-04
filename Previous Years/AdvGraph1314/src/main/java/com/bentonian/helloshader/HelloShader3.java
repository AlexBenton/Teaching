package com.bentonian.helloshader;

import static java.lang.Math.PI;

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
 * This class draws a sphere in perspective.  The sphere is shaded with
 * Gouraud shading.  The code demonstrates using a vertex array ovject
 * to bind two distinct vertex buffers (one comprising vertex coordinate
 * data, the other vertex normal data) together at rendering time.
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
public class HelloShader3 implements GLEventListener {

  private static final int DU = 40;
  private static final int DV = 40;
  private static final float[][][] SPHERE_CORNERS = new float[DU][DV][3];
  private static final int DATA_SIZE = (DU - 1) * (DV - 1) * (4 + 2);
  static {
    for (int u = 0; u < DU; u++) {
      for (int v = 0; v < DV; v++) {
        float s = (float) (2 * PI * u / DU);
        float t = (float) ((PI * v / DV) - (PI / 2));
        SPHERE_CORNERS[u][v][0] = (float) (Math.cos(s) * Math.cos(t));
        SPHERE_CORNERS[u][v][1] = (float) (Math.sin(t));
        SPHERE_CORNERS[u][v][2] = (float) (Math.sin(s) * Math.cos(t));
      }
    }
  }

  private static final String vertexShader = Joiner.on("\n").join(
    "#version 330",
    "",
    "uniform mat4 modelToScreen;",
    "layout(location = 0) in vec3 vPosition;",
    "layout(location = 1) in vec3 vNormal;",
    "out float diffuse;",
    "",
    "void main() {",
    "  vec3 light = vec3(10, 10, 10);",
    "  gl_Position = modelToScreen * vec4(vPosition, 1);",
    "  diffuse = dot(vNormal, normalize(light - vPosition));",
    "}"
  );

  private static final String fragmentShader = Joiner.on("\n").join(
    "#version 330",
    "",
    "in float diffuse;",
    "out vec4 color;",
    "",
    "void main() {",
    "  vec3 purple = vec3(0.8, 0.8, 1);",
    "  color = vec4(purple * diffuse, 1);",
    "}"
  );

  private final int[] vertexArrayId = new int[1];
  private final int[] vertexBufferIds = new int[2];

  @Override
  public void init(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);

    int program = createAndInstallProgram(gl);
    applyPerspectiveTransform(gl, program);
    fillVertexArray(gl);
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

  private void fillVertexArray(GL4 gl) {

    // Fill Java FloatBuffers with the points on a sphere.
    FloatBuffer vertices = Buffers.newDirectFloatBuffer(DATA_SIZE * 3);
    FloatBuffer normals = Buffers.newDirectFloatBuffer(DATA_SIZE * 3);
    for (int u = 0; u < DU - 1; u++) {
      for (int v = 0; v < DV - 1; v++) {
        // Choose face normal.  This isn't exact, but it's simple.
        float[] normal = SPHERE_CORNERS[u][v];
        
        // First triangle of the square face
        for (int[] step : new int[][] { {0,0}, {1,0}, {1,1} }) {
          vertices.put(SPHERE_CORNERS[u + step[0]][v + step[1]]);
          normals.put(normal);
        }
        // Second triangle of the square face
        for (int[] step : new int[][] { {0,0}, {1,1}, {0,1} }) {
          vertices.put(SPHERE_CORNERS[u + step[0]][v + step[1]]);
          normals.put(normal);
        }
      }
    }
    vertices.rewind();
    normals.rewind();
    
    // Translate all vertices -3 along the Z axis
    for (int i = 0; i < DATA_SIZE; i++) {
      vertices.put(i*3 + 2, vertices.get(i*3 + 2) - 3);
    }

    // Create and bind a Vertex Array Object
    gl.glGenVertexArrays(1, vertexArrayId, 0);
    gl.glBindVertexArray(vertexArrayId[0]);

    // Create two OpenGL vertex buffer objects
    gl.glGenBuffers(2, vertexBufferIds, 0);

    // Bind the first vertex buffer object.
    // Pass in the vertex FloatBuffer.
    // Tell OpenGL to pass the current vertex buffer object to parameter 0 in the shader.
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferIds[0]);
    gl.glBufferData(GL.GL_ARRAY_BUFFER, Floats.BYTES * DATA_SIZE * 3, vertices, GL.GL_STATIC_DRAW);
    gl.glEnableVertexAttribArray(0);
    gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, 0, 0);

    // Bind the second vertex buffer object.
    // Pass in the normals FloatBuffer.
    // Tell OpenGL to pass the current vertex buffer object to parameter 1 in the shader.
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferIds[1]);
    gl.glBufferData(GL.GL_ARRAY_BUFFER, Floats.BYTES * DATA_SIZE * 3, normals, GL.GL_STATIC_DRAW);
    gl.glEnableVertexAttribArray(1);
    gl.glVertexAttribPointer(1, 3, GL.GL_FLOAT, false, 0, 0);
    
    // Unbind the vertex buffer
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
  }

  @Override
  public void display(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    gl.glDrawArrays(GL.GL_TRIANGLES, 0, DATA_SIZE);
  }

  @Override
  public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
  }

  @Override
  public void dispose(GLAutoDrawable glDrawable) {
    GL4 gl = glDrawable.getGL().getGL4();
    gl.glBindVertexArray(0);
    gl.glDeleteVertexArrays(1, vertexArrayId, 0);
    gl.glDeleteBuffers(2, vertexBufferIds, 0);
  }

  public static void main(String[] args) {
    HelloShaderAWTFrame.wrap("Hello Sphere", new HelloShader3());
  }
}
