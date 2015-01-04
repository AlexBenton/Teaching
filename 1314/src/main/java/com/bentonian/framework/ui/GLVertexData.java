package com.bentonian.framework.ui;

import static com.bentonian.framework.mesh.VertexReader.COLOR_READER;
import static com.bentonian.framework.mesh.VertexReader.NORMAL_READER;
import static com.bentonian.framework.mesh.VertexReader.POSITION_READER;
import static com.bentonian.framework.mesh.VertexReader.TEXTURE_COORDS_READER;

import java.nio.FloatBuffer;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.Vertex;
import com.bentonian.framework.mesh.VertexReader;
import com.bentonian.framework.mesh.textures.TexCoord;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Floats;
import com.jogamp.common.nio.Buffers;

public class GLVertexData {

  public static enum Mode {
    NONE,
    TRIANGLES,
    QUADS,
    LINE_SEGMENTS,
    LINE_TRIANGLES,
    LINE_QUADS,
  }

  private final Mode glMode;
  private final int[] vertexArrayId = new int[1];
  private final int[] vertexBufferIds = new int[4];

  private GL4 gl;
  int vPosition, vNormal, vColor, vTexCoord, enableTexturing, texture;
  private boolean isCompiled;
  
  private List<Vertex> vertices;
  private M3d normal;
  private M3d color;
  private int textureId;
  private TexCoord textureCoordinates;

  /////////////////////////////////////////////////////////////////////////////

  public GLVertexData(Mode glMode) {
    this.glMode = glMode;
    this.vertices = Lists.newArrayList();
    this.normal = new M3d(0, 0, 0);
    this.color = new M3d(1, 1, 1);
    this.textureId = -1;
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

  /////////////////////////////////////////////////////////////////////////////

  public boolean isCompiled() {
    return isCompiled;
  }

  public GLVertexData vertex(M3d point) {
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

  public GLVertexData normal(M3d normal) {
    Preconditions.checkState(!isCompiled);
    this.normal = new M3d(normal);
    return this;
  }

  public GLVertexData color(M3d color) {
    Preconditions.checkState(!isCompiled);
    this.color = new M3d(color);
    return this;
  }

  public GLVertexData texture(int textureId) {
    Preconditions.checkState(!isCompiled);
    this.textureId = textureId;
    return this;
  }

  public GLVertexData textureCoordinates(TexCoord tc) {
    Preconditions.checkState(!isCompiled);
    this.textureCoordinates = tc;
    return this;
  }

  public GLVertexData render(GLRenderingContext glRenderingContext) {
    this.gl = glRenderingContext.getGl();
    if (!isCompiled) {
      compile(glRenderingContext);
    }

    boolean useTexturing = (textureId != -1) && (enableTexturing != -1) && (texture != -1);

    if (useTexturing) {
      gl.glBindTexture(GL4.GL_TEXTURE_2D, textureId);
      gl.glUniform1i(enableTexturing, 1);
      gl.glUniform1i(texture, 0);
    }
    if (needLineOffset()) {
      glRenderingContext.pushLineOffset();
    }

    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glDrawArrays(getGlMode(), 0, vertices.size());
    gl.glBindVertexArray(0);

    if (needLineOffset()) {
      glRenderingContext.popLineOffset();
    }
    if (useTexturing) {
      gl.glBindTexture(GL4.GL_TEXTURE_2D, 0);
      gl.glUniform1i(enableTexturing, 0);
    }

    return this;
  }

  public void dispose() {
    if (isCompiled()) {
      gl.glDeleteBuffers(4, vertexBufferIds, 0);
      gl.glDeleteVertexArrays(1, vertexArrayId, 0);
      isCompiled = false;
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private void compile(GLRenderingContext glRenderingContext) {
    int program = glRenderingContext.getProgram();

    // Find program fields
    vPosition = gl.glGetAttribLocation(program, "vPosition");
    vNormal = gl.glGetAttribLocation(program, "vNormal");
    vColor = gl.glGetAttribLocation(program, "vColor");
    vTexCoord = gl.glGetAttribLocation(program, "vTexCoord");
    enableTexturing = gl.glGetUniformLocation(program, "enableTexturing");
    texture = gl.glGetUniformLocation(program, "texture");

    // Create and bind a Vertex Array Object
    gl.glGenVertexArrays(1, vertexArrayId, 0);
    gl.glBindVertexArray(vertexArrayId[0]);

    // Add vertex buffer objects to the VAO
    gl.glGenBuffers(4, vertexBufferIds, 0);
    copyDataToAttribute(gl, vertices, vPosition, vertexBufferIds[0], POSITION_READER);
    copyDataToAttribute(gl, vertices, vNormal, vertexBufferIds[1], NORMAL_READER);
    copyDataToAttribute(gl, vertices, vColor, vertexBufferIds[2], COLOR_READER);
    if (textureId != -1) {
      copyDataToAttribute(gl, vertices, vTexCoord, vertexBufferIds[3], TEXTURE_COORDS_READER);
    }

    gl.glBindVertexArray(0);
    for (int attribId : new int[] { vPosition, vNormal, vColor, vTexCoord }) {
      if (attribId != -1) {
        gl.glDisableVertexAttribArray(attribId);
      }
    }
    isCompiled = true;
  }

  private void copyDataToAttribute(GL4 gl, List<Vertex> vertices, int attributeId, int bufferId,
      VertexReader reader) {
    if (attributeId != -1) {
      int numFloats = reader.asFloats(Iterables.getLast(vertices)).length;
      FloatBuffer data = Buffers.newDirectFloatBuffer(vertices.size() * numFloats);

      for (Vertex v : vertices) {
        data.put(reader.asFloats(v));
      }
      data.rewind();

      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferId);
      gl.glBufferData(GL.GL_ARRAY_BUFFER, vertices.size() * numFloats * Floats.BYTES, data, GL.GL_STATIC_DRAW);
      gl.glEnableVertexAttribArray(attributeId);
      gl.glVertexAttribPointer(attributeId, numFloats, GL.GL_FLOAT, false, 0, 0);
      gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }
  }

  private boolean needLineOffset() {
    return getGlMode() == GL.GL_LINES;
  }

  private int getGlMode() {
    switch (glMode) {
    case LINE_SEGMENTS:
    case LINE_TRIANGLES:
    case LINE_QUADS:
      return GL.GL_LINES;
    default:
      return GL.GL_TRIANGLES;
    }
  }
}
