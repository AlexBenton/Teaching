package com.bentonian.gldemos.sdfpolygonization;

import static com.bentonian.framework.io.FileUtil.readFileOrExit;
import static com.bentonian.framework.math.MathUtil.midPt;
import static com.bentonian.gldemos.sdfpolygonization.SceneConstants.ROOT;
import static com.bentonian.gldemos.sdfpolygonization.SceneConstants.USER_SCENE;
import static com.bentonian.gldemos.sdfpolygonization.SceneConstants.VS;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.implicits.ForceFunction;
import com.bentonian.framework.mesh.implicits.ImplicitSurfaceRefiner;
import com.bentonian.framework.mesh.implicits.Octree;
import com.bentonian.framework.mesh.implicits.OctreeConstants;
import com.bentonian.framework.mesh.implicits.Sample;
import com.bentonian.framework.mesh.primitive.MeshPrimitiveWithTexture;
import com.bentonian.framework.mesh.primitive.Square;
import com.bentonian.framework.texture.BufferedImageTexture;
import com.bentonian.framework.ui.GLCanvas;
import com.bentonian.framework.ui.GLFrameBuffer;
import com.bentonian.framework.ui.ShaderUtil;
import com.google.common.collect.ImmutableSet;

public class GPUImplicitSurfaceRefiner extends ImplicitSurfaceRefiner {

  private static final String EVAL_FS_BASE = readFileOrExit(ROOT + "eval-base.fsh");
  private static final double DR = 3;
  private static final Vec3 MIN = new Vec3(-DR);
  private static final Vec3 MAX = new Vec3(DR);
  private static final int EVAL_BUFFER_WIDTH = 400;
  private static final int EVAL_BUFFER_HEIGHT = 400;
  
  private final GLCanvas gl;
  private final GLFrameBuffer evalBuffer;
  private final ByteBuffer instructionBuffer;
  private final MeshPrimitiveWithTexture evalCanvas;
  private final Vec3[] realPositions;

  private int evalProgram = -1;
  private int evalBufferPos = 0;
  private boolean onRenderThread = false;
  private int evalBufferTextureId = -1;

  public GPUImplicitSurfaceRefiner(GLCanvas gl) {
    super(MIN, MAX, null /* force function */);
    
    this.forceFunction = new ForceFunction() {
      @Override
      public double getCutoff() {
        return 0.0;
      }
      @Override
      public Sample sample(Vec3 v) {
        Sample s = getCached(v);
        if (s == null) {
          System.out.println("Cache miss: [" + v + "]");
          throw new IllegalAccessError();
        }
        return s;
      }
      @Override
      public boolean isHot(Sample sample) {
        return sample.getForce() < 0;
      }
      @Override 
      public Set<Vec3> getTargets() {
        return ImmutableSet.of(new Vec3(0));
      }
    };

    this.gl = gl;
    this.evalCanvas = new Square().setTexture(new BufferedImageTexture(SDFPolygonizationDemo.class, "background.jpg"));
    this.evalBuffer = new GLFrameBuffer(EVAL_BUFFER_WIDTH, EVAL_BUFFER_HEIGHT);
    this.instructionBuffer = ByteBuffer.allocateDirect(EVAL_BUFFER_WIDTH * 3 * EVAL_BUFFER_HEIGHT * 4);
    this.realPositions = new Vec3[EVAL_BUFFER_WIDTH * EVAL_BUFFER_HEIGHT];
  }

  @Override
  protected void findRootOctrees() {
    if (onRenderThread) {
      for (int i = 0; i <= fx; i++) {
        for (int j = 0; j <= fy; j++) {
          for (int k = 0; k <= fz; k++) {
            addEvalPt(new Vec3(
                min.getX() + i * scale, min.getY() + j * scale, min.getZ() + k * scale));
          }
        }
      }
      eval();
      super.findRootOctrees();
    }
  }

  @Override
  protected void refineInProgress(long timeout) {
    onRenderThread = true;

    if (roots.isEmpty()) {
      findRootOctrees();
    }

    while (System.currentTimeMillis() < timeout) {
      if (evalProgram != -1 && !inProgress.isEmpty()) {
        for (Octree o : inProgress) {
          evalSubcorners(o);
        }
        eval();
      }
      
      List<Octree> workingSet = new ArrayList<>();
      workingSet.addAll(inProgress);
      inProgress.clear();
      for (Octree o : workingSet) {
        refine(o);
      }
    }

    onRenderThread = false;
  }
  
  private void evalSubcorners(Octree o) {
    for (int edge = 0; edge < 12; edge++) {
      int[][] endPoints = OctreeConstants.EDGES[edge];
      addEvalPt(midPt(o.getCorner(endPoints[0]), o.getCorner(endPoints[1])));
    }

    for (int face = 0; face < 6; face++) {
      int[][] faceIndices = OctreeConstants.FACES[face];
      addEvalPt(midPt(o.getCorner(faceIndices[0]), o.getCorner(faceIndices[2])));
    }

    addEvalPt(midPt(o.getCorner(0, 0, 0), o.getCorner(1, 1, 1)));
  }

  private void resetEvalBuffer() {
    evalBufferPos = 0;
  }

  private void addEvalPt(Vec3 v) {
    appendToByteBuffer(instructionBuffer, evalBufferPos * 3 + 0, v.getX());
    appendToByteBuffer(instructionBuffer, evalBufferPos * 3 + 1, v.getY());
    appendToByteBuffer(instructionBuffer, evalBufferPos * 3 + 2, v.getZ());
    realPositions[evalBufferPos++] = v;
    
    if (evalBufferPos == EVAL_BUFFER_WIDTH * EVAL_BUFFER_HEIGHT) {
      eval();
    }
  }
  
  private void appendToByteBuffer(ByteBuffer buffer, int pos, double d) {
    int e7 = (int) (d * 10000000);

    buffer.put(pos * 4 + 0, (byte) ((e7 >> 24) & 0xFF));
    buffer.put(pos * 4 + 1, (byte) ((e7 >> 16) & 0xFF));
    buffer.put(pos * 4 + 2, (byte) ((e7 >> 8) & 0xFF));
    buffer.put(pos * 4 + 3, (byte) ((e7 >> 0) & 0xFF));
  }

  private void eval() {
    if (evalBufferPos > 0) {
      gl.pushProgram(evalProgram);
      gl.pushFrameBuffer(evalBuffer);
      GL11.glViewport(0, 0, EVAL_BUFFER_WIDTH, EVAL_BUFFER_HEIGHT);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
      updateInstructionBuffers();
      evalCanvas.render(gl);
      gl.popFrameBuffer(evalBuffer);
      gl.popProgram();
  
      ByteBuffer bytes = extractBytes(evalBuffer);
      for (int i = 0; i < evalBufferPos; i++) {
        Vec3 v = realPositions[i];
        float f = bytes.getInt(i * 4) / 10000000.0f;
        forceFunction.addToCache(v, new Sample(v, f));
        if (forceFunction.getCached(v) == null) {
          throw new IllegalAccessError();
        }
      }
      resetEvalBuffer();
    }
  }
  
  private void updateInstructionBuffers() {
    int active = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
    
    if (evalBufferTextureId == -1) {
      evalBufferTextureId = GL11.glGenTextures();
    }
    
    GL13.glActiveTexture(GL13.GL_TEXTURE2);
    GL20.glUniform1i(GL20.glGetUniformLocation(evalProgram, "evalBuffer"), 2);
    GLCanvas.updateTextureBuffer(evalBufferTextureId, instructionBuffer, 
        EVAL_BUFFER_WIDTH * 3, EVAL_BUFFER_HEIGHT, GL11.GL_RGBA, GL11.GL_LINEAR);
    
    GL13.glActiveTexture(active);
  }

  private static ByteBuffer extractBytes(GLFrameBuffer framebuffer) {
    // Create and fill a ByteBuffer with the frame data.
    ByteBuffer pixels = ByteBuffer.allocateDirect(framebuffer.getWidth() * framebuffer.getHeight() * 4);
    GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebuffer.getTextureId());
    GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
    return pixels;
  }

  public void reloadShader() {
    int vs = ShaderUtil.loadShader(GL20.GL_VERTEX_SHADER, VS);

    String userScene = readFileOrExit(ROOT + USER_SCENE);
    String evalShader = EVAL_FS_BASE + "\n\n" + userScene;
    int fs = ShaderUtil.loadShader(GL20.GL_FRAGMENT_SHADER, evalShader, ROOT);

    evalProgram = ShaderUtil.compileProgram(vs, fs);
    reset();
  }
}
