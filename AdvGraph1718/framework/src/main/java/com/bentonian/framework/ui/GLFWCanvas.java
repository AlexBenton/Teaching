package com.bentonian.framework.ui;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * The specifics of the LWJGL implementation--setup and shutdown, key codes, etc. 
 */
public class GLFWCanvas extends GLWindowedCanvas {

  private long window;

  public GLFWCanvas(String title) {
    super(title);
  }

  @Override
  protected void initGl() {
    initGLFW();
    initGLFWInput();
    super.initGl();
  }

  private void initGLFW() {
    GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
    GLFW.glfwSetErrorCallback(errorCallback);
    if (!GLFW.glfwInit()) {
      throw new IllegalStateException("Failed to initialize GLFW");
    }
    GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
    window = GLFW.glfwCreateWindow(width, height, title, NULL, NULL);
    if (window == 0) {
      throw new RuntimeException("Failed to create window");
    }
    GLFW.glfwSetWindowSizeCallback(window, (window, width, height) -> { 
      onResized(width, height);
    });
    GLFW.glfwMakeContextCurrent(window);
    GLFW.glfwSwapInterval(1);
    GLFW.glfwShowWindow(window);
    GL.createCapabilities();
  }

  @Override
  public void shutdownGl() {
    super.shutdownGl();
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
    GLFW.glfwSetErrorCallback(null).free();
    window = 0;
  }

  @Override
  protected void preDraw() {
    GLFW.glfwPollEvents();
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
  }

  @Override
  protected void postDraw() {
    GLFW.glfwSwapBuffers(window);
  }

  @Override
  public void setTitle(String title) {
    super.setTitle(title);
    if (window != 0) {
      GLFW.glfwSetWindowTitle(window, this.title);
    }
  }

  private void initGLFWInput() {
    GLFW.glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if (key == GLFW.GLFW_KEY_LEFT_CONTROL || key == GLFW.GLFW_KEY_RIGHT_CONTROL) {
        isControlDown = (action != GLFW.GLFW_RELEASE);
      }

      switch (action) {
      case GLFW.GLFW_PRESS:
        onKeyDown(key);
        break;
      case GLFW.GLFW_RELEASE:
        onKeyUp(key);
        break;
      }
    });

    GLFW.glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
      if (button == GLFW.GLFW_MOUSE_BUTTON_1) {
        DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
        DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);
        GLFW.glfwGetCursorPos(window, xpos, ypos);
        int x = (int) xpos.get();
        int y = (int) ypos.get();

        if (action == GLFW.GLFW_PRESS) {
          isLeftMouseDown = true;
          deliverOnMouseDown(x, y);
        } else if (action == GLFW.GLFW_RELEASE) {
          isLeftMouseDown = false;
          deliverOnMouseUp(x, y);
        }
      }
    });

    GLFW.glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
      if (isLeftMouseDown) {
        deliverOnMouseDrag((int) xpos, (int) ypos);
      } else {
        deliverOnMouseMove((int) xpos, (int) ypos);
      }
    });

    GLFW.glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
      onMouseWheel((int) yoffset);
    });
  }

  @Override
  public boolean isExitRequested() {
    return glfwWindowShouldClose(window) || super.isExitRequested();
  }

  @Override
  public GLWindowedCanvas setWindowPos(int left, int top, int width, int height) {
    if (window != 0) {
      GLFW.glfwSetWindowSize(window, width, height);
      GLFW.glfwSetWindowPos(window, left, top);
    }
    return super.setWindowPos(left, top, width, height);
  }

  @Override
  protected void onResized(int width, int height) {
    super.onResized(width, height);
    if (window != 0) {
      GL11.glViewport(0, 0, width, height);
      updateProjectionMatrix();
    }
  }
}
