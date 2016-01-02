package com.bentonian.gldemos.raytracedtexture;

import java.awt.Graphics;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Circle;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.Cylinder;
import com.bentonian.framework.mesh.primitive.MeshPrimitiveWithTexture;
import com.bentonian.framework.mesh.primitive.Sphere;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.mesh.primitive.Torus;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.raytrace.engine.Scene;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.texture.Texture;
import com.bentonian.framework.ui.BufferedImageRGBCanvas;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLWindowedAppSecondaryFrame;
import com.google.common.collect.ImmutableList;

public class RayTracedTextureDemo extends DemoApp {
  
  private static final Primitive BASE = new Circle().translate(new M3d(0, -1.25, 0));

  private final Texture[] textures = new Texture[] {
      new BufferedImageTexture(RayTracedTextureDemo.class, "opengl.png"),
      new BufferedImageTexture(RayTracedTextureDemo.class, "earth.png"),
  };
  private final MeshPrimitiveWithTexture[] meshes = new MeshPrimitiveWithTexture[] {
    new Cube(),
    new Sphere(),
    new Square(),
    new Torus(),
    new Cylinder()
  };

  private int currentTexture = 0;
  private int currentMesh = 0;
  
  private Scene scene;
  private GLWindowedAppSecondaryFrame output;
  private RayTracerEngine engine;
  private BufferedImageRGBCanvas canvas;

  protected RayTracedTextureDemo() {
    super("Textures");

    this.scene = new Scene() {
      @Override
      public List<Primitive> getPrimitives() {
        return ImmutableList.of(meshes[currentMesh], BASE);
      }
    };
    this.scene.addLight(new M3d(10, 10, 10));
    this.scene.addLight(new M3d(0, 10, 0));

    this.canvas = new BufferedImageRGBCanvas(32, 32);
    this.engine = new RayTracerEngine(scene, canvas, camera);

    this.output = new GLWindowedAppSecondaryFrame(this, "Ray traced scene") {
      @Override
      public void paint(Graphics g) {
        g.drawImage(canvas, getInnerLeft(), getInnerTop(), this);
      }
      @Override
      public void onResized() {
        super.onResized();
        canvas = new BufferedImageRGBCanvas(getInnerWidth(), getInnerHeight());
        engine.setCanvas(canvas);
      }
    };
    
    setCameraDistance(5);
    getCamera().translate(getCamera().getDirection().times(5));
    getCamera().rotate(new M3d(1, 0, 0), Math.PI / 4);
    getCamera().rotate(new M3d(0, 1, 0), Math.PI / 4);
    getCamera().translate(getCamera().getDirection().times(-5));
    
    for (MeshPrimitiveWithTexture mesh : meshes) {
      mesh.getFeaturesAccelerator().setShowEdges(true);
    }

    meshes[currentMesh].setTexture(textures[currentTexture]);
  }

  @Override
  public void initGl() {
    super.initGl();
    output.setInnerSize(400 * Display.getWidth() / Display.getHeight(), 400);
    output.setLocation(Display.getX() + Display.getWidth() + 14, Display.getY());
    output.setVisible(true);
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case Keyboard.KEY_R:
      engine.renderToCanvas();
      output.repaint();
      break;
    case Keyboard.KEY_MINUS:
      currentTexture = (currentTexture + textures.length - 1) % textures.length;
      meshes[currentMesh].setTexture(textures[currentTexture]);
      break;
    case Keyboard.KEY_EQUALS:
      currentTexture = (currentTexture + 1) % textures.length;
      meshes[currentMesh].setTexture(textures[currentTexture]);
      break;
    case Keyboard.KEY_LBRACKET:
      currentMesh = (currentMesh + meshes.length - 1) % meshes.length;
      meshes[currentMesh].setTexture(textures[currentTexture]);
      break;
    case Keyboard.KEY_RBRACKET:
      currentMesh = (currentMesh + 1) % meshes.length;
      meshes[currentMesh].setTexture(textures[currentTexture]);
      break;
    }
    super.onKeyDown(key);
  }

  @Override
  public void draw() {
    meshes[currentMesh].render(this);
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new RayTracedTextureDemo().run();
  }
}
