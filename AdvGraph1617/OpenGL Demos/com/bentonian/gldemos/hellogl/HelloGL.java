package com.bentonian.gldemos.hellogl;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class HelloGL {

  public static void main(String[] args) {

    ///////////////////////////////////////////////////////////////////////////
    // Set up GLFW window

    GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
    GLFW.glfwSetErrorCallback(errorCallback);
    GLFW.glfwInit();
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
    GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
    long window = GLFW.glfwCreateWindow(800 /* width */, 600 /* height */, "HelloGL", 0, 0);
    GLFW.glfwMakeContextCurrent(window);
    GLFW.glfwSwapInterval(1);
    GLFW.glfwShowWindow(window);

    ///////////////////////////////////////////////////////////////////////////
    // Set up OpenGL

    GL.createCapabilities();
    GL11.glClearColor(0.2f, 0.4f, 0.6f, 0.0f);
    GL11.glClearDepth(1.0f);

    ///////////////////////////////////////////////////////////////////////////
    // Set up minimal shader programs

    // Vertex shader source
    String[] vertex_shader = {
      "#version 330\n",
      "in vec3 v;",
      "void main() {",
      "  gl_Position = vec4(v, 1.0);",
      "}"
    };

    // Fragment shader source
    String[] fragment_shader = {
        "#version 330\n",
        "out vec4 frag_color;",
        "void main() {",
        "  frag_color = vec4(1.0, 1.0, 1.0, 1.0);",
        "}"
    };

    // Compile vertex shader
    int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
    GL20.glShaderSource(vs, vertex_shader);
    GL20.glCompileShader(vs);
    
    // Compile fragment shader
    int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
    GL20.glShaderSource(fs, fragment_shader);
    GL20.glCompileShader(fs);
    
    // Link vertex and fragment shaders into an active program
    int program = GL20.glCreateProgram();
    GL20.glAttachShader(program, vs);
    GL20.glAttachShader(program, fs);
    GL20.glLinkProgram(program);
    GL20.glUseProgram(program);
    
    ///////////////////////////////////////////////////////////////////////////
    // Set up data

    // Fill a Java FloatBuffer object with memory-friendly floats
    float[] coords = new float[] { -0.5f, -0.5f, 0,  0, 0.5f, 0,  0.5f, -0.5f, 0 };
    FloatBuffer fbo = BufferUtils.createFloatBuffer(coords.length);
    fbo.put(coords);                                // Copy the vertex coords into the floatbuffer
    fbo.flip();                                     // Mark the floatbuffer ready for reads

    // Store the FloatBuffer's contents in a Vertex Buffer Object
    int vbo = GL15.glGenBuffers();                  // Get an OGL name for the VBO
    GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);   // Activate the VBO
    GL15.glBufferData(GL15.GL_ARRAY_BUFFER, fbo, GL15.GL_STATIC_DRAW);  // Send VBO data to GPU

    // Bind the VBO in a Vertex Array Object
    int vao = GL30.glGenVertexArrays();             // Get an OGL name for the VAO
    GL30.glBindVertexArray(vao);                    // Activate the VAO
    GL20.glEnableVertexAttribArray(0);              // Enable the VAO's first attribute (0)
    GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);  // Link VBO to VAO attrib 0

    ///////////////////////////////////////////////////////////////////////////
    // Loop until window is closed

    while (!GLFW.glfwWindowShouldClose(window)) {
      GLFW.glfwPollEvents();

      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
      GL30.glBindVertexArray(vao);
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0 /* start */, 3 /* num vertices */);

      GLFW.glfwSwapBuffers(window);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Clean up

    GL15.glDeleteBuffers(vbo);
    GL30.glDeleteVertexArrays(vao);
    GLFW.glfwDestroyWindow(window);
    GLFW.glfwTerminate();
    GLFW.glfwSetErrorCallback(null).free();
  }
}
