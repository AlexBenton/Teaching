package com.bentonian.framework.ui;

import static com.bentonian.framework.ui.VertexReader.COLOR_READER;
import static com.bentonian.framework.ui.VertexReader.NORMAL_READER;
import static com.bentonian.framework.ui.VertexReader.POSITION_READER;
import static com.bentonian.framework.ui.VertexReader.TEXTURE_COORDS_READER;

import java.nio.FloatBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import com.bentonian.framework.math.MathConstants;
import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.texture.TexCoord;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * This class emulates the original OpenGL 1.1-style immediate mode graphics,
 * simplifying cache management and texture attachment to 'compile' instances of
 * geometry into vertex array objects.
 */
public class GLVertexData {

  public static enum Mode {
    NONE, TRIANGLES, QUADS, LINE_SEGMENTS, LINE_TRIANGLES, LINE_QUADS,
  }

  private final Mode glMode;

  private int vPosition, vNormal, vColor, vTexCoord, enableTexturing, texture;
  private int vertexArrayId;
  private int positionVertexBufferId, normalVertexBufferId, colorVertexBufferId,
      texCoordsVertexBufferId;
  private boolean isCompiled;

  private List<Vertex> vertices;
  private Vec3 normal;
  private Vec3 color;
  private boolean hasTexture;
  private TexCoord textureCoordinates;

  // ///////////////////////////////////////////////////////////////////////////

  public GLVertexData(Mode glMode) {
    this.glMode = glMode;
    this.vertices = Lists.newArrayList();
    this.normal = MathConstants.ORIGIN;
    this.color = new Vec3(1, 1, 1);
    this.hasTexture = false;
    this.isCompiled = false;
  }

  public static GLVertexData beginQuads() {
    return new GLVertexData(GLVertexData.Mode.QUADS);
  }

  public static GLVertexData beginTriangles() {
    return new GLVertexData(GLVertexData.Mode.TRIANGLES);
  }

  public static GLVertexData beginLineSegments() {
    return new GLVertexData(GLVertexData.Mode.LINE_SEGMENTS);
  }

  public static GLVertexData beginLineTriangles() {
    return new GLVertexData(GLVertexData.Mode.LINE_TRIANGLES);
  }

  public static GLVertexData beginLineQuads() {
    return new GLVertexData(GLVertexData.Mode.LINE_QUADS);
  }

  // ///////////////////////////////////////////////////////////////////////////

  public boolean isCompiled() {
    return isCompiled;
  }

  public boolean hasTexture() {
    return hasTexture;
  }

  /**
   * This implementation is NOT OPTIMAL: quads are converted into pairs of
   * triangles instead of triangle strips, and triangle and line strips are
   * unsupported.
   */
  public GLVertexData vertex(Vec3 point) {
    Preconditions.checkState(!isCompiled);
    Vertex v = new Vertex(point);
    v.setNormal(normal);
    v.setColor(color);
    v.setTextureCoords(textureCoordinates);
    vertices.add(v);
    switch (glMode) {
    case QUADS:
      if (vertices.size() % 6 == 4) {
        vertices.add(new Vertex(vertices.get(vertices.size() - 4)));
        vertices.add(new Vertex(vertices.get(vertices.size() - 3)));
      }
      break;
    case LINE_TRIANGLES:
      if (vertices.size() % 6 == 2) {
        vertices.add(new Vertex(vertices.get(vertices.size() - 1)));
      } else if (vertices.size() % 6 == 4) {
        vertices.add(new Vertex(vertices.get(vertices.size() - 1)));
        vertices.add(new Vertex(vertices.get(vertices.size() - 5)));
      }
      break;
    case LINE_QUADS:
      if ((vertices.size() % 8 == 2) || (vertices.size() % 8 == 4)) {
        vertices.add(new Vertex(vertices.get(vertices.size() - 1)));
      } else if (vertices.size() % 8 == 6) {
        vertices.add(new Vertex(vertices.get(vertices.size() - 1)));
        vertices.add(new Vertex(vertices.get(vertices.size() - 7)));
      }
    default:
      break;
    }
    return this;
  }

  public GLVertexData normal(Vec3 normal) {
    Preconditions.checkState(!isCompiled);
    this.normal = new Vec3(normal);
    return this;
  }

  public GLVertexData color(Vec3 color) {
    Preconditions.checkState(!isCompiled);
    this.color = new Vec3(color);
    return this;
  }

  public GLVertexData setHasTexture(boolean hasTexture) {
    Preconditions.checkState(!isCompiled);
    this.hasTexture = hasTexture;
    return this;
  }

  public GLVertexData textureCoordinates(TexCoord tc) {
    Preconditions.checkState(!isCompiled);
    this.textureCoordinates = tc;
    return this;
  }

  public GLVertexData render(GLCanvas glCanvas) {
    if (!isCompiled) {
      compile(glCanvas);
    }

    boolean useTexturing = hasTexture && (enableTexturing != -1) && (texture != -1);

    if (useTexturing) {
      GL20.glUniform1i(enableTexturing, 1);
      GL20.glUniform1i(texture, 0);
    }
    if (needLineOffset()) {
      glCanvas.pushLineOffset();
    }

    GL30.glBindVertexArray(vertexArrayId);
    GL11.glDrawArrays(getGlMode(), 0, vertices.size());
    GL30.glBindVertexArray(0);

    if (needLineOffset()) {
      glCanvas.popLineOffset();
    }
    if (useTexturing) {
      GL20.glUniform1i(enableTexturing, 0);
    }

    return this;
  }

  public void dispose() {
    if (isCompiled()) {
      GL15.glDeleteBuffers(positionVertexBufferId);
      GL15.glDeleteBuffers(normalVertexBufferId);
      GL15.glDeleteBuffers(colorVertexBufferId);
      GL15.glDeleteBuffers(texCoordsVertexBufferId);
      GL30.glDeleteVertexArrays(vertexArrayId);
      vertices.clear();
      isCompiled = false;
    }
  }

  // ///////////////////////////////////////////////////////////////////////////

  private void compile(GLCanvas glCanvas) {
    int program = glCanvas.getProgram();

    // Find program fields
    vPosition = GL20.glGetAttribLocation(program, "vPosition");
    vNormal = GL20.glGetAttribLocation(program, "vNormal");
    vColor = GL20.glGetAttribLocation(program, "vColor");
    vTexCoord = GL20.glGetAttribLocation(program, "vTexCoord");
    enableTexturing = GL20.glGetUniformLocation(program, "enableTexturing");
    texture = GL20.glGetUniformLocation(program, "texture");

    // Create and bind a Vertex Array Object
    vertexArrayId = GL30.glGenVertexArrays();
    GL30.glBindVertexArray(vertexArrayId);

    // Add vertex buffer objects to the VAO
    positionVertexBufferId = GL15.glGenBuffers();
    normalVertexBufferId = GL15.glGenBuffers();
    colorVertexBufferId = GL15.glGenBuffers();
    texCoordsVertexBufferId = GL15.glGenBuffers();
    copyDataToAttribute(vertices, vPosition, positionVertexBufferId, POSITION_READER);
    copyDataToAttribute(vertices, vNormal, normalVertexBufferId, NORMAL_READER);
    copyDataToAttribute(vertices, vColor, colorVertexBufferId, COLOR_READER);
    if (hasTexture) {
      copyDataToAttribute(vertices, vTexCoord, texCoordsVertexBufferId, TEXTURE_COORDS_READER);
    }

    GL30.glBindVertexArray(0);
    for (int attribId : new int[] { vPosition, vNormal, vColor, vTexCoord }) {
      if (attribId != -1) {
        GL20.glDisableVertexAttribArray(attribId);
      }
    }
    isCompiled = true;
  }

  private void copyDataToAttribute(List<Vertex> vertices, int attributeId, int bufferId,
      VertexReader reader) {
    if (attributeId != -1 && !vertices.isEmpty()) {
      int numFloats = reader.asFloats(Iterables.getLast(vertices)).length;
      FloatBuffer data = BufferUtils.createFloatBuffer(vertices.size() * numFloats);

      for (Vertex v : vertices) {
        data.put(reader.asFloats(v));
      }
      data.flip();

      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, bufferId);
      GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);
      GL20.glEnableVertexAttribArray(attributeId);
      GL20.glVertexAttribPointer(attributeId, numFloats, GL11.GL_FLOAT, false, 0, 0);
      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }
  }

  private boolean needLineOffset() {
    return getGlMode() == GL11.GL_LINES;
  }

  private int getGlMode() {
    switch (glMode) {
    case LINE_SEGMENTS:
    case LINE_TRIANGLES:
    case LINE_QUADS:
      return GL11.GL_LINES;
    default:
      return GL11.GL_TRIANGLES;
    }
  }
}
