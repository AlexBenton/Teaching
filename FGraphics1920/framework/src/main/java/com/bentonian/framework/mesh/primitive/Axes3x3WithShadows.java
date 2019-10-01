package com.bentonian.framework.mesh.primitive;

import static com.bentonian.framework.math.MathConstants.X_AXIS;
import static com.bentonian.framework.math.MathConstants.Y_AXIS;
import static com.bentonian.framework.math.MathConstants.Z_AXIS;

import com.bentonian.framework.material.Colors;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.scene.Primitive;
import com.bentonian.framework.scene.PrimitiveCollection;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.texture.ShadowTexture;

public class Axes3x3WithShadows extends PrimitiveCollection {

  public Axes3x3WithShadows(Primitive shadowSource) {
    this(shadowSource, Colors.WHITE);
  }

  public Axes3x3WithShadows(Primitive shadowSource, Vec3 color) {
    Square plane;

    plane = new Square();
    plane.scale(3);
    plane.translate(new Vec3(0, 0, -3));
    plane.setTexture(
        new ShadowTexture(shadowSource, plane, BufferedImageTexture.AXES, Z_AXIS, color)
            .enableOutlineOnly());
    add(plane);

    plane = new Square();
    plane.scale(3);
    plane.rotate(Y_AXIS, -Math.PI / 2);
    plane.translate(new Vec3(-3, 0, 0));
    plane.setTexture(
        new ShadowTexture(shadowSource, plane, BufferedImageTexture.AXES, X_AXIS, color)
            .enableOutlineOnly());
    add(plane);

    plane = new Square();
    plane.scale(3);
    plane.rotate(X_AXIS, Math.PI / 2);
    plane.translate(new Vec3(0, -3, 0));
    plane.setTexture(
        new ShadowTexture(shadowSource, plane, BufferedImageTexture.AXES, Y_AXIS, color)
            .enableOutlineOnly());
    add(plane);
  }
}
