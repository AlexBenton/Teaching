package com.bentonian.framework.mesh.primitive;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
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

  protected boolean isTextured() {
    return texture != null;
  }

  @Override
  protected Material getMaterial(M3d pt) {
    return applyTextureToMaterial(pt, getMaterial());
  }

  protected M3d applyTextureToNormal(M3d pt, M3d normal) {
    return isTextured() ? texture.getNormal(this, pt, normal) : normal;
  }

  protected Material applyTextureToMaterial(M3d pt, Material material) {
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
