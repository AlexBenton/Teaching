package com.bentonian.gldemos.raytracedtexture;

import static com.bentonian.framework.io.FileUtil.loadImageResource;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.primitive.Circle;
import com.bentonian.framework.mesh.primitive.Cube;
import com.bentonian.framework.mesh.primitive.MeshPrimitiveWithTexture;
import com.bentonian.framework.raytrace.engine.RayTracerEngine;
import com.bentonian.framework.raytrace.engine.Scene;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.texture.IsTextured;
import com.bentonian.framework.texture.TexCoord;
import com.bentonian.framework.texture.Texture;
import com.bentonian.framework.ui.BufferedImageRGBCanvas;
import com.bentonian.framework.ui.DemoApp;
import com.bentonian.framework.ui.GLWindowedAppSecondaryFrame;

public class RayTracedTextureDemo extends DemoApp {

  private Scene scene;
  private GLWindowedAppSecondaryFrame output;
  private RayTracerEngine engine;
  private BufferedImageRGBCanvas canvas;
  private Texture texture;
  private double low, high;
  private boolean edgy = false;

  protected RayTracedTextureDemo() {
    super("Textures");

    final MeshPrimitiveWithTexture box = 
        (MeshPrimitiveWithTexture) new Cube().scale(new M3d(13, 1, 1));
    
    this.low = 0.02;
    this.high = 0.035;
    
    this.scene = new Scene();
    this.scene.addLight(new M3d(-10, 10, 10));
    this.scene.addLight(new M3d(0, 10, 0));
    this.scene.add(box);
    this.scene.add(new Circle().scale(new M3d(5, 5, 5)).translate(new M3d(0, -1.25, 0)));

    this.canvas = new BufferedImageRGBCanvas(32, 32);
    this.engine = new RayTracerEngine(scene, canvas, camera);
    
    BufferedImage image = loadImageResource(getClass().getResourceAsStream("alphabet.png"));
    final SignedDistanceField sdf = new SignedDistanceField(image);
    this.texture = new BufferedImageTexture(image) {
      @Override
      public M3d getColor(IsTextured target, M3d pt) {
        if (box.isCompiled()) {
          // if d < -0.005 then t = 0; 
          // if d > 0.005 then t = 1; 
          // else t interpolates the small interval.
          TexCoord tc = target.getTextureCoord(pt);
          double d = sdf.sample(tc);
          double t = smoothstep(low, high, d);
          double u = smoothstep(high, high + 0.025, d);
          M3d outer = edgy ? Texture.ORANGE : Texture.WHITE;
          return (d <= high || !edgy) ? Texture.BLACK.times(1 - t).plus(outer.times(t))
              : outer.times(1 - u).plus(Texture.WHITE.times(u));
        } else {
          return super.getColor(target, pt);
        }
      }

      @Override
      public void bind() {
        super.bind();
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
      }
    };

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

    setCameraDistance(15);
    box.getFeaturesAccelerator().setShowEdges(true);
    box.setTexture(texture);
  }

  private double smoothstep(double left, double right, double t) {
    t = (t - left) / (right - left);
    t = max(0, min(t, 1));
    return t*t*(3 - 2*t);
  }

  @Override
  public void initGl() {
    super.initGl();
    output.setInnerSize(400 * getWidth() / getHeight(), 400);
    output.setLocation(getLeft() + getWidth() + 14, getTop());
    output.setVisible(true);
  }

  @Override
  public void onKeyDown(int key) {
    switch (key) {
    case GLFW.GLFW_KEY_R:
      raytrace();
      break;
    case GLFW.GLFW_KEY_E:
      edgy = !edgy;
      break;
    case GLFW.GLFW_KEY_4:
      animateCameraToPosition(new M3d(-5 * sin(1.72 * PI), 0, 5 * cos(1.72 * PI)));
      break;
    case GLFW.GLFW_KEY_LEFT_BRACKET:
      low -= 0.005;
      System.out.println("SDF smoothing range: [" + low + ", " + high + "]");
      break;
    case GLFW.GLFW_KEY_RIGHT_BRACKET:
      low += 0.005;
      System.out.println("SDF smoothing range: [" + low + ", " + high + "]");
      break;
    case GLFW.GLFW_KEY_MINUS:
      high -= 0.005;
      System.out.println("SDF smoothing range: [" + low + ", " + high + "]");
      break;
    case GLFW.GLFW_KEY_EQUAL:
      high += 0.005;
      System.out.println("SDF smoothing range: [" + low + ", " + high + "]");
      break;
    }
    super.onKeyDown(key);
  }
  
  private void raytrace() {
    if (isControlDown()) {
      engine.setSupersamplingMultiple(4);
    }
    engine.renderToCanvas();
    engine.setSupersamplingMultiple(1);
    output.repaint();
  }

  @Override
  public void draw() {
    scene.render(this);
  }

  /////////////////////////////////////////////////////////////////////////////

  public static void main(String[] args) {
    new RayTracedTextureDemo().run();
  }
}
