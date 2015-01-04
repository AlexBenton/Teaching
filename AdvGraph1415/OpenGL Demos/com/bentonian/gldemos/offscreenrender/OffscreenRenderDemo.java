package com.bentonian.gldemos.offscreenrender;

import org.lwjgl.opengl.GL11;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.texture.Texture;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLFrameBuffer;

public class OffscreenRenderDemo extends DemoApp {

  private static final Primitive SPHERE = new Sphere().translate(new M3d(2, 0, 0));
  private static final Primitive BRICK_CUBE = new Cube()
      .setTexture(BufferedImageTexture.BRICK)
      .rotate(new M3d(1, 0, 0), Math.PI / 4)
      .rotate(new M3d(0, 1, 0), Math.PI / 4);

  private final Cube frameBufferCube;
  private final GLFrameBuffer frameBuffer;
  
  protected OffscreenRenderDemo() {
    super("Offscreen Rendering");

    this.frameBuffer = new GLFrameBuffer(256, 256);
    this.frameBufferCube = new Cube();

    setCameraDistance(5);
    frameBufferCube.getFeaturesAccelerator().setShowEdges(true);
    frameBufferCube.rotate(new M3d(1, 0, 0), Math.PI / 4);
    frameBufferCube.rotate(new M3d(0, 1, 0), Math.PI / 4);
    frameBufferCube.setTexture(new Texture() {
      @Override
      public void bind() {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, frameBuffer.getTextureId());
      }      
    });
  }

  @Override
  public void preDraw() {
    pushFrameBuffer(frameBuffer);
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    BRICK_CUBE.rotate(new M3d(0, 1, 0), 0.01);
    BRICK_CUBE.render(this);
    SPHERE.render(this);
    popFrameBuffer(frameBuffer);
    
    super.preDraw();
  }
  
  @Override
  public void draw() {
    frameBufferCube.rotate(new M3d(0, 1, 0), 0.01);
    frameBufferCube.render(this);
    SPHERE.render(this);
  }
  
  public static void main(String[] args) {
    new OffscreenRenderDemo().run();
  }
}
