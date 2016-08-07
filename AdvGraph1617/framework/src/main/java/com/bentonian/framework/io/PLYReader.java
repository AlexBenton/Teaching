package com.bentonian.framework.io;

import java.io.InputStream;

import com.bentonian.framework.math.M3d;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.ui.Vertex;
import com.google.common.base.Joiner;

public class PLYReader {
  
  // This code has not yet been proven to work.

  public static Mesh parseFile(String filename) {
    return parse(Joiner.on("").join(FileUtil.readFile(filename)));
  }

  public static Mesh parseResource(InputStream resource) {
    return parse(Joiner.on("").join(FileUtil.readResource(resource)));
  }

  public static Mesh parse(String data) {
    String chunk;
    StringTokenizer tokenizer, tokie;
    Mesh mesh = new Mesh();
    String vertexCount = null;
    String faceCount = null;

    tokenizer = new StringTokenizer(data);
    chunk = tokenizer.next('\n');
    if (!chunk.equalsIgnoreCase("ply")) {
      throw new UnsupportedOperationException("Ill-formatted PLY data read: " + chunk);
    }

    chunk = tokenizer.next('\n');
    if (!chunk.equalsIgnoreCase("format ascii 1.0")) {
      throw new UnsupportedOperationException("Unsupported PLY encoding found: " + chunk);
    }

    while (!chunk.equalsIgnoreCase("end_header")) {
      chunk = tokenizer.next('\n');
      System.out.println(chunk);
      tokie = new StringTokenizer(chunk);
      if (tokie.next(' ').equalsIgnoreCase("element")) {
        String next = tokie.next(' ');
        if (next.equalsIgnoreCase("vertex")) {
          vertexCount = tokie.next(' ');
        } else if (next.equalsIgnoreCase("face")) {
          faceCount = tokie.next(' ');
        }
      }
    }

    if ((vertexCount == null) || (faceCount == null)) {
      throw new UnsupportedOperationException("Missing vertex or face count");
    }

    int nVerts = Integer.valueOf(vertexCount);
    int nFaces = Integer.valueOf(faceCount);

    Vertex verts[] = new Vertex[nVerts];
    for (int i = 0; i < nVerts; i++) {
      tokie = new StringTokenizer(tokenizer.next('\n'));
      verts[i] = new Vertex(new M3d(
          Double.valueOf(tokie.next(' ')),
          Double.valueOf(tokie.next(' ')),
          Double.valueOf(tokie.next(' '))));
    }

    for (int i = 0; i < nFaces; i++) {
      tokie = new StringTokenizer(tokenizer.next('\n'));
      int n = Integer.valueOf(tokie.next(' '));
      Vertex[] f = new Vertex[n];
      for (int j = 0; j < n; j++) {
        f[j] = verts[Integer.valueOf(tokie.next(' '))];
      }
      mesh.add(new MeshFace(f));
    }

    mesh.computeAllNormals();
    return mesh;
  }
}
