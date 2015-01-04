package com.bentonian.jogldemos.texture;

import static com.bentonian.framework.io.FileUtil.loadImageResource;
import static com.bentonian.framework.io.ShaderUtil.compileProgram;
import static com.bentonian.framework.io.ShaderUtil.loadShader;

import javax.media.opengl.GL4;
import javax.media.opengl.GLAutoDrawable;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.mesh.advanced.TexturedSquare;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.mesh.textures.ImageTexture;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.jogldemos.internals.JoglDemo;
import com.bentonian.jogldemos.internals.JoglDemoContainer;

public class TextureDemo extends JoglDemo {

  private static final Primitive COLORED_SQUARE = new Square() {
    @Override
    protected void addFace() {
      normal(Z_AXIS);
      for (int i = 0; i < 4; i++) {
        M3d pt = MathConstants.CORNERS_OF_A_SQUARE[i];
        color(new M3d((pt.getX() + 1) / 2.0, (pt.getY() + 1) / 2.0, 1));
        vertex(pt);
      }
    }
  }.translate(new M3d(0, 0, -0.0001));
  private static final Primitive MAP_SQUARE = new TexturedSquare(new ImageTexture(
      loadImageResource(TextureDemo.class.getResourceAsStream("earth.png"))));

  protected TextureDemo() {
    super("Textures");
  }

  @Override
  public void init(GLAutoDrawable glDrawable) {
    super.init(glDrawable);

    int vShader = loadShader(gl, GL4.GL_VERTEX_SHADER, TextureDemo.class, "texture.vsh");
    int fShader = loadShader(gl, GL4.GL_FRAGMENT_SHADER, TextureDemo.class, "texture.fsh");
    int shaderProgram = compileProgram(gl, vShader, fShader);
    useProgram(shaderProgram);

    setCameraDistance(3);
  }

  @Override
  public void draw() {
    COLORED_SQUARE.render(this);
    MAP_SQUARE.render(this);
  }

  public static void main(String[] args) {
    JoglDemoContainer.go(new TextureDemo());
  }
}
