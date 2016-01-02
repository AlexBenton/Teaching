package com.bentonian.framework.ui;

import static org.lwjgl.opengl.GL11.GL_INT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30.glFramebufferTexture2D;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30.glRenderbufferStorage;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/**
 * Offscreen OpenGL rendering target.
 *
 * Borrowed without remorse from jherico's glamour library (https://github.com/jherico/glamour)
 */
public class GLFrameBuffer {

  private final int width, height;

  private int frameBuffer = 0;
  private int textureId = 0;

  private IntBuffer oldViewport;
  private int oldFrameBuffer;

  public GLFrameBuffer(int width, int height) {
    this.width = width;
    this.height = height;
    this.oldViewport = BufferUtils.createIntBuffer(16);
  }

  public void dispose() {
    if (textureId != 0) {
      GL11.glDeleteTextures(textureId);
      textureId = 0;
    }
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public float getAspectRatio() {
    return width / (float) height;
  }

  public void activate() {
    oldViewport.rewind();
    GL11.glGetInteger(GL11.GL_VIEWPORT, oldViewport);
    oldFrameBuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

    if (frameBuffer == 0) {
      frameBuffer = glGenFramebuffers();
      glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer );

      if (textureId == 0) {
        textureId = glGenTextures();
      }
      glBindTexture(GL_TEXTURE_2D, textureId);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE );
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE );

      glTexImage2D(
          GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0,GL_RGBA, GL_INT, (java.nio.ByteBuffer) null);
      glFramebufferTexture2D(GL_FRAMEBUFFER,GL_COLOR_ATTACHMENT0,GL_TEXTURE_2D, textureId, 0);

      int depthBuffer = glGenRenderbuffers();
      glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
      glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, width, height);
      glFramebufferRenderbuffer(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_RENDERBUFFER, depthBuffer);
    }

    glViewport(0, 0, width, height);
    glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
  }

  public void deactivate() {
    glBindFramebuffer(GL_FRAMEBUFFER, oldFrameBuffer);
    glViewport(oldViewport.get(), oldViewport.get(), oldViewport.get(), oldViewport.get());
  }

  public int getTextureId() {
    if (textureId == 0) {
      textureId = glGenTextures();
    }
    return textureId;
  }
}
