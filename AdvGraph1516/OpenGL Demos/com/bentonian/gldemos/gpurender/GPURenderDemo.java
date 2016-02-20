package com.bentonian.gldemos.gpurender;

import static com.bentonian.framework.ui.ShaderUtil.compileProgram;
import static com.bentonian.framework.ui.ShaderUtil.loadShader;
import static com.bentonian.framework.ui.ShaderUtil.testGlError;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.scene.Camera;
import com.bentonian.framework.ui.DemoApp;

public class GPURenderDemo extends DemoApp {

  private static final String[] SHADERS = { "refraction.fsh", "primitives.fsh", "noise.fsh", "sdf.fsh", "sdf-blobbies.fsh" };
  
  private final Square square;

  private int program;
  private Camera frozenCamera;
  private boolean showRenderDepth;
  private boolean paused;
  private int currentShader;
  private long elapsed, lastTick;

  protected GPURenderDemo() {
    super("GPU Render");
    this.program = -1;
    this.square = new Square();
    this.square.setHasTexture(true);
    this.currentShader = 0;
    this.paused = false;
    this.elapsed = 0;
    this.lastTick = System.currentTimeMillis();

    setCameraDistance(2.15);
    this.frozenCamera = new Camera(getCamera());
    animateCameraToPosition(new M3d(20, 10, 20));
  }

  @Override
  protected void initGl() {
    super.initGl();
    loadShaderProgram(SHADERS[currentShader]);
  }

  @Override
  protected Camera getCameraForModelview() {
    return frozenCamera;
  }

  @Override
  public void draw() {
    M3d camPos = getCamera().getPosition();
    M3d camDir = getCamera().getDirection();
    M3d camUp = getCamera().getUp();
    long now = System.currentTimeMillis();

    if (!paused) {
      elapsed += now - lastTick;
    }
    lastTick = now;
    updateUniformFloat("iGlobalTime", ((float) elapsed) / 1000.0f);
    updateUniformVec3("iRayOrigin", camPos);
    updateUniformVec3("iRayDir", camDir);
    updateUniformVec3("iRayUp", camUp);
    square.render(this);
  }

  @Override
  protected void onResized(int width, int height) {
    super.onResized(width, height);
    square.setIdentity();
    square.scale(new M3d(width / (float) height, 1, 1));
    if (program != -1) {
      updateUniformVec2("iResolution", (float) width, (float) height);
    }
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case Keyboard.KEY_R:
      showRenderDepth = !showRenderDepth;
      updateUniformBoolean("iShowRenderDepth", showRenderDepth);
      break;
    case Keyboard.KEY_4:
      animateCameraToPosition(new M3d(2, 1, 2).normalized().times(8));
      break;
    case Keyboard.KEY_LBRACKET:
      currentShader = (currentShader + SHADERS.length - 1) % SHADERS.length;
      loadShaderProgram(SHADERS[currentShader]);
      break;
    case Keyboard.KEY_RBRACKET:
      currentShader = (currentShader + 1) % SHADERS.length;
      loadShaderProgram(SHADERS[currentShader]);
      break;
    case Keyboard.KEY_SPACE:
      paused = !paused;
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  private void loadShaderProgram(String fragmentShaderFilename) {
    int vsName = loadShader(GL20.GL_VERTEX_SHADER, GPURenderDemo.class, "basic.vsh");
    int fsName = loadShader(GL20.GL_FRAGMENT_SHADER, GPURenderDemo.class, fragmentShaderFilename);

    program = compileProgram(vsName, fsName);
    testGlError();
    useProgram(program);
    updateUniformVec2("iResolution", (float) getWidth(), (float) getHeight());
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new GPURenderDemo().run();
  }
}
