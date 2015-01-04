package com.bentonian.framework.ui;

import static com.bentonian.framework.io.ShaderUtil.compileProgram;
import static com.bentonian.framework.io.ShaderUtil.loadShader;
import static com.bentonian.framework.io.ShaderUtil.validateLocation;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.bentonian.framework.io.ShaderUtil;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.math.MatrixStack;
import com.bentonian.framework.scene.Camera;

/**
 * Interface for OpenGL adapter layer
 *
 * @author Alex Benton
 */
public abstract class GLRenderingContext implements GLEventListener {

  protected GL4 gl;

  private final Camera camera;
  private final MatrixStack projection;
  private final MatrixStack modelStack;

  private int program;
  
  public GLRenderingContext() {
    this.modelStack = new MatrixStack();
    this.projection = new MatrixStack();
    this.camera = new Camera();

    projection.push(M4x4.perspective(1.0));
  }

  @Override
  public void init(GLAutoDrawable glDrawable) {
    this.gl = glDrawable.getGL().getGL4();
    testGlError();

    int vsName = loadShader(gl, GL4.GL_VERTEX_SHADER, GLRenderingContext.class, "default.vsh");
    int fsName = loadShader(gl, GL4.GL_FRAGMENT_SHADER, GLRenderingContext.class, "default.fsh");
    int shader = compileProgram(gl, vsName, fsName);
    useProgram(shader);

    testGlError();
    gl.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);
    gl.glClearDepth(1.0f);
    gl.glEnable(GL.GL_DEPTH_TEST);
    gl.glDepthFunc(GL.GL_LEQUAL);
    gl.glEnable(GL.GL_BLEND);
    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_NEAREST);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_NEAREST);
    testGlError();
  }

  @Override
  public void display(GLAutoDrawable glDrawable) {
    this.gl = glDrawable.getGL().getGL4();
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
    updateUniforms();
    preDraw();
    draw();
    postDraw();
    gl.glFlush();
    this.gl = null;
  }

  protected void preDraw() {
  }

  protected void draw() {
  }

  protected void postDraw() {
  }

  public GL4 getGl() {
    return gl;
  }

  @Override
  public void dispose(GLAutoDrawable glDrawable) {
  }

  @Override
  public void reshape(GLAutoDrawable glDrawable, int x, int y, int width, int height) {
    this.gl = glDrawable.getGL().getGL4();
    height = Math.max(1, height);
    gl.glViewport(0, 0, width, height);
    testGlError();
    projection.peek().setData(M4x4.perspective((float) width / (float) height).getData());
    updateProjectionMatrix();
  }

  public void push(M4x4 T) {
    modelStack.push(T);
    updateUniforms();
  }

  public void pop() {
    modelStack.pop();
    updateUniforms();
  }

  public M4x4 peek() {
    return modelStack.peek();
  }

  public void pushLineOffset() {
    projection.pushReversed(M4x4.translationMatrix(new M3d(0, 0, -0.0001)));
    updateProjectionMatrix();
  }

  public void popLineOffset() {
    projection.pop();
    updateProjectionMatrix();
  }
  
  public int setupTexture(BufferedImage texture) {
    int[] textureName = new int[1];

    // Get the image into a known byte alignment.
    // Could this be done more efficiently?  Heck yes!
    ByteBuffer buffer = ByteBuffer.allocate(texture.getWidth() * texture.getHeight() * 4);
    for (int x = 0; x < texture.getWidth(); x++) {
      for (int y = 0; y < texture.getHeight(); y++) {
        int argb = texture.getRGB(x, y);
        buffer.put((x + y * texture.getWidth()) * 4 + 0, (byte) ((argb >> 16) & 0xFF));
        buffer.put((x + y * texture.getWidth()) * 4 + 1, (byte) ((argb >>  8) & 0xFF));
        buffer.put((x + y * texture.getWidth()) * 4 + 2, (byte) ((argb >>  0) & 0xFF));
        buffer.put((x + y * texture.getWidth()) * 4 + 3, (byte) ((argb >> 24) & 0xFF));
      }
    }

    gl.glEnable(GL.GL_TEXTURE_2D);
    gl.glGenTextures(1, textureName, 0);
    gl.glBindTexture(GL4.GL_TEXTURE_2D, textureName[0]);
    gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, texture.getWidth(), texture.getHeight(), 0,
        GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
    gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);

    return textureName[0];
  }

  public void useProgram(int program) {
    this.program = program;
    gl.glUseProgram(program);
    testGlError();
  }

  public int getProgram() {
    return program;
  }

  public Camera getCamera() {
    return camera;
  }

  public int getUniformLocation(String name) {
    return validateLocation(gl.glGetUniformLocation(program, name), name);
  }

  public int getInputLocation(String name) {
    return validateLocation(gl.glGetAttribLocation(program, name), name);
  }
  
  protected void updateUniforms() {
    M4x4 modelToWorld = peek();
    M4x4 modelToCamera = camera.getParentToLocal().times(modelToWorld);

    testGlError();
    updateM4x4("modelToWorld", modelToWorld);
    updateM4x4("modelToCamera", modelToCamera);
    updateM3x3("normalToWorld", modelToWorld.extract3x3().inverted().transposed());
    updateM3x3("normalToCamera", modelToCamera.times(modelToWorld).extract3x3().inverted().transposed());
    updateM4x4("modelToScreen", projection.peek().times(modelToCamera));

    updateVec3("eyePosition", camera.getPosition());
    updateVec3("lightDirection", getLightDirection());
  }

  private void updateProjectionMatrix() {
    updateM4x4("modelToScreen", projection.peek().times(camera.getParentToLocal().times(peek())));
  }

  protected M3d getLightDirection() {
    return camera.getPosition().normalized();
  }

  private void updateM4x4(String uniformName, M4x4 T) {
    int uniform = gl.glGetUniformLocation(program, uniformName);
    if (uniform != -1) {
      gl.glUniformMatrix4fv(uniform, 1, false, T.asFloats(), 0);
      testGlError();
    }
  }

  private void updateM3x3(String uniformName, M4x4 T) {
    int uniform = gl.glGetUniformLocation(program, uniformName);
    if (uniform != -1) {
      gl.glUniformMatrix3fv(uniform, 1, false, T.as3x3Floats(), 0);
      testGlError();
    }
  }

  private void updateVec3(String uniformName, M3d v) {
    int uniform = gl.glGetUniformLocation(program, uniformName);
    if (uniform != -1) {
      gl.glUniform3f(uniform, (float) v.getX(), (float) v.getY(), (float) v.getZ());
      testGlError();
    }
  }

  protected void testGlError() {
    ShaderUtil.testGlError(gl);
  }
}
