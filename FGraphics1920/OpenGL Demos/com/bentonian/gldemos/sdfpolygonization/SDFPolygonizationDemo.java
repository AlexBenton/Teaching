package com.bentonian.gldemos.sdfpolygonization;

import static com.bentonian.framework.io.FileUtil.readFileOrExit;
import static com.bentonian.framework.ui.ShaderUtil.loadShader;
import static com.bentonian.gldemos.sdfpolygonization.SceneConstants.ROOT;
import static com.bentonian.gldemos.sdfpolygonization.SceneConstants.USER_SCENE;
import static com.bentonian.gldemos.sdfpolygonization.SceneConstants.VS;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.implicits.ImplicitSurfaceMesh;
import com.bentonian.framework.mesh.primitive.MeshPrimitiveWithTexture;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.ShaderAutoloader;
import com.bentonian.framework.ui.ShaderUtil;

public class SDFPolygonizationDemo extends DemoApp {
  
  public static final String FS_BASE = readFileOrExit(ROOT + "base.fsh");

  private final MeshPrimitiveWithTexture glslCanvas;
  private final GPUImplicitSurfaceRefiner refiner;
  private final ImplicitSurfaceMesh surface;

  private ShaderAutoloader loader;
  private int glslProgram = -1;

  protected SDFPolygonizationDemo() {
    super("SDF Polygonization");
    
    width = 1200;
    height = 1800;

    this.glslCanvas = new Square().setTexture(new BufferedImageTexture(SDFPolygonizationDemo.class, "background.jpg"));
    this.loader = new ShaderAutoloader(
        new String[] { ROOT },
        () -> loadShader(GL20.GL_VERTEX_SHADER, VS),
        () -> buildSceneShader(),
        () -> exitRequested,
        (p) -> onSceneShaderLoaded(p),
        (e) -> System.err.println(e)
    );

    this.refiner = new GPUImplicitSurfaceRefiner(this);
    this.surface = new ImplicitSurfaceMesh(refiner)
        .setTargetLevel(5);
    surface.setShowEdges(true);
    
    animateCameraToPosition(new Vec3(0, 0, 5));
  }
  
  @Override
  public void initGl() {
    super.initGl();
    GL11.glDisable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_DITHER);
  }

  @Override
  public void preDraw() {
    loader.preDraw();
    super.preDraw();
  }
  
  @Override
  public void draw() {
    useProgram(glslProgram);
    GL11.glViewport(0, getHeight() / 2, getWidth(), getHeight() / 2);
    updateUniformVec3("iRayOrigin", getCamera().getPosition());
    updateUniformVec3("iRayDir", getCamera().getDirection());
    updateUniformVec3("iRayUp", getCamera().getUp());
    updateUniformVec2("iResolution", getWidth(), (float) getHeight() / 2.0f);
    glslCanvas.render(this);
    
    useProgram(DEFAULT_SHADER_PROGRAM);
    GL11.glViewport(0, 0, getWidth(), getHeight() / 2);
    surface.render(this);
  }

  @Override
  protected void onResized(int width, int height) {
    super.onResized(width, height);
    getProjection().peek().setData(M4x4.perspective((float) width / (float) (height / 2.0)));
  }
  
  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case GLFW.GLFW_KEY_B:
      surface.setShowBoxes(!surface.getShowBoxes());
      break;
    case GLFW.GLFW_KEY_E:
      surface.setShowEdges(!surface.getShowEdges());
      break;
    case GLFW.GLFW_KEY_F:
      surface.setShowFaces(!surface.getShowFaces());
      break;
    case GLFW.GLFW_KEY_EQUAL:
      surface.setTargetLevel(Math.min(10, surface.getTargetLevel() + 1));
      break;
    case GLFW.GLFW_KEY_MINUS:
      surface.setTargetLevel(Math.max(2, surface.getTargetLevel() - 1));
      break;
    default: super.onKeyDown(key);
      break;
    }
  }

  private int buildSceneShader() {
    String userScene = readFileOrExit(ROOT + USER_SCENE);
    String scene = FS_BASE + "\n\n" + userScene;
    return ShaderUtil.loadShader(GL20.GL_FRAGMENT_SHADER, scene, ROOT);
  }
  
  private void onSceneShaderLoaded(Integer p) {
    glslProgram = p;
    refiner.reloadShader();
  }
  
  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new SDFPolygonizationDemo().run();
  }
}
