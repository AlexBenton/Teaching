package com.bentonian.gldemos.geometryshader;

import static com.bentonian.framework.ui.ShaderUtil.checkProgram;
import static com.bentonian.framework.ui.ShaderUtil.loadShader;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.io.File;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import com.bentonian.framework.io.OFFUtil;
import com.bentonian.framework.mesh.primitive.MeshPrimitive;
import com.bentonian.framework.ui.DemoApp;


public class GeometryShaderDemo extends DemoApp {

  private MeshPrimitive bunny;
  private long shaderTimestamp = 0;

  public GeometryShaderDemo() {
    super("Geometry Shader");
    setCameraDistance(3);
    this.bunny = new MeshPrimitive(OFFUtil.parseFile("bunny.off"));
  }

  @Override
  public void initGl() {
    super.initGl();
    maybeReloadShader();
  }

  @Override
  public void draw() {
    maybeReloadShader();
    bunny.render(this);
  }

  private void maybeReloadShader() {
    long timestamp = getAllShadersTimestamps();
    if (shaderTimestamp != timestamp) {
      loadProgram();
      shaderTimestamp = timestamp;
    }
  }

  private long getAllShadersTimestamps() {
    try {
      return new File("com/bentonian/gldemos/geometryshader/vert.vsh").lastModified()
          ^ new File("com/bentonian/gldemos/geometryshader/frag.fsh").lastModified()
          ^ new File("com/bentonian/gldemos/geometryshader/geom.gsh").lastModified();
    } catch (Exception e) {
      return -1;
    }
  }

  private void loadProgram() {
    try {
      int vShader = load(GL20.GL_VERTEX_SHADER, "com/bentonian/gldemos/geometryshader/vert.vsh");
      int gShader = load(GL32.GL_GEOMETRY_SHADER, "com/bentonian/gldemos/geometryshader/geom.gsh");
      int fShader = load(GL20.GL_FRAGMENT_SHADER, "com/bentonian/gldemos/geometryshader/frag.fsh");
      int program = glCreateProgram();
  
      glAttachShader(program, vShader);
      glAttachShader(program, gShader);
      glAttachShader(program, fShader);
      glLinkProgram(program);
  
      glValidateProgram(program);
      checkProgram(program);
      useProgram(program);
      System.out.println("Shaders loaded");
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }
  
  private static int load(int shaderType, String file) {
    try {
      return loadShader(shaderType, file);
    } catch (Exception e) {
      throw new RuntimeException(file + ":\n" + e.getMessage());
    }
  }

  public static void main(String[] args) {
    new GeometryShaderDemo().run();
  }
}
