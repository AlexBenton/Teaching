package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.texture.IsTextured;
import com.bentonian.framework.texture.Texture;
import com.bentonian.framework.ui.GLCanvas;

public abstract class MeshPrimitiveWithTexture extends MeshPrimitive implements IsTextured {

  private Texture texture;

  protected MeshPrimitiveWithTexture() {
    super(new Mesh());
  }

  public MeshPrimitiveWithTexture setTexture(Texture texture) {
    this.texture = texture;
    setHasTexture(texture != null);
    return this;
  }

  @Override
  public MeshPrimitiveWithTexture setIdentity() {
    super.setIdentity();
    return this;
  }

  @Override
  public MeshPrimitiveWithTexture translate(Vec3 v) {
    super.translate(v);
    return this;
  }

  @Override
  public MeshPrimitiveWithTexture rotate(Vec3 axis, double d) {
    super.rotate(axis, d);
    return this;
  }

  @Override
  public MeshPrimitiveWithTexture scale(Vec3 v) {
    super.scale(v);
    return this;
  }

  @Override
  public MeshPrimitiveWithTexture scale(double d) {
    super.scale(d);
    return this;
  }

  protected boolean isTextured() {
    return texture != null;
  }

  @Override
  protected Material getMaterial(Vec3 pt) {
    return applyTextureToMaterial(pt, getMaterial());
  }

  protected Vec3 applyTextureToNormal(Vec3 pt, Vec3 normal) {
    return isTextured() ? texture.getNormal(this, pt, normal) : normal;
  }

  protected Material applyTextureToMaterial(Vec3 pt, Material material) {
    return isTextured() ? texture.getMaterial(this, pt, material) : material;
  }

  @Override
  protected void renderLocal(GLCanvas glCanvas) {
    if (texture != null) {
      texture.bind();
    }
    super.renderLocal(glCanvas);
    if (texture != null) {
      texture.unbind();
    }
  }

  @Override
  public void dispose() {
    if (texture != null) {
      texture.dispose();
    }
    super.dispose();
  }

  @Override
  protected void renderVertex(MeshFace face, int index) {
    textureCoordinates(getTextureCoord(face.get(index)));
    super.renderVertex(face, index);
  }
}
