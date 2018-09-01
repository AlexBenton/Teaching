package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathConstants.NEGATIVE_Z_AXIS;
import static com.bentonian.framework.math.MathConstants.Y_AXIS;
import static com.bentonian.framework.math.MathConstants.Z_AXIS;
import static java.awt.Color.WHITE;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.math.M4x4;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.ui.GLCanvas;

/**
 * Text readout locked to the upper-left-hand corner of the window.
 */
public class PinnedText extends Square {

  private final Graphics2D g;

  public PinnedText() {
    BufferedImageTexture t = new BufferedImageTexture(256, 256);

    g = t.getBufferedImage().createGraphics();
    g.setFont(new Font("Arial", Font.PLAIN, 40));
    g.setColor(WHITE);

    scale(0.25);
    translate(new M3d(-0.75, 0.75, 0));
    setTexture(t);
  }

  public void setText(String text) {
    g.setComposite(AlphaComposite.Clear); 
    g.fillRect(0, 0, 256, 256); 
    g.setComposite(AlphaComposite.SrcOver);

    g.drawString(text, 10, 40);
    dispose();
  }

  @Override
  public void render(GLCanvas canvas) {
    canvas.pushProgram(GLCanvas.DEFAULT_SHADER_PROGRAM);
    canvas.getCamera().push(new M4x4());
    canvas.getCamera().lookAt(Z_AXIS, NEGATIVE_Z_AXIS, Y_AXIS);

    M4x4 P = canvas.peekProjection();
    double xScale = P.getData()[0][0];
    double yScale = P.getData()[1][1];
    double d = 1 - (yScale / xScale);

    translate(new M3d(d, 0, 0));
    scale(1 / yScale);
    super.render(canvas);
    scale(yScale);
    translate(new M3d(-d, 0, 0));

    canvas.getCamera().pop();
    canvas.popProgram();
  }
}
