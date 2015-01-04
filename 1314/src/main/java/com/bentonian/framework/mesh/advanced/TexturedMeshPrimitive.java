package com.bentonian.framework.mesh.advanced;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import com.bentonian.framework.material.Material;
import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.mesh.primitive.MeshPrimitive;
import com.bentonian.framework.mesh.textures.IsTextured;
import com.bentonian.framework.mesh.textures.Texture;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.bentonian.framework.ui.GLRenderingContext;

public class TexturedMeshPrimitive extends MeshPrimitive implements IsTextured {

  protected Texture texture;
  
  public TexturedMeshPrimitive(Mesh source, Texture texture) {
    super(source);
    this.texture = texture;
  }

  @Override
  public TexCoord getTextureCoord(M3d pt) {
    // Sphere texture coordinates
    double u = 0.5 + atan2(pt.getZ(), pt.getX()) / (2 * PI);
    double v = 0.5 - asin(pt.getY()) / PI;
    return new TexCoord(u, v);
  }
  
  @Override
  public M3d getUBasis(M3d pt) {
    // Sphere texture coordinates
    double theta = atan2(pt.getZ(), pt.getX());
    return new M3d(cos(theta + PI / 2), 0, sin(theta + PI / 2));
  }

  @Override
  protected Material getMaterial(M3d pt) {
    return texture.getMaterial(this, pt, super.getMaterial(pt));
  }

  @Override
  protected void addTriangle(GLRenderingContext glCanvas, Vertex A, Vertex B, Vertex C) {
    textureCoordinates((A.getTexCoords() != null) ? A.getTexCoords() : getTextureCoord(A));
    vertex(A);
    textureCoordinates((B.getTexCoords() != null) ? B.getTexCoords() : getTextureCoord(B));
    vertex(B);
    textureCoordinates((A.getTexCoords() != null) ? C.getTexCoords() : getTextureCoord(C));
    vertex(C);
  }
}
