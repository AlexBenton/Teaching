package com.bentonian.framework.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;

import com.bentonian.framework.math.Vec3;
import com.bentonian.framework.mesh.Mesh;
import com.bentonian.framework.mesh.MeshFace;
import com.bentonian.framework.mesh.MeshVertex;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.BiMap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBiMap;

public class OFFUtil {

  public static Mesh parseFile(String filename) {
    return parse(Joiner.on("").join(FileUtil.readFile(filename)));
  }

  public static Mesh parseResource(InputStream resource) {
    return parse(Joiner.on("").join(FileUtil.readResource(resource)));
  }

  public static Mesh parse(String data) {
    String chunk;
    StringTokenizer tokenizer, tokie;
    int nVerts, nFaces;
    Mesh mesh = new Mesh();

    tokenizer = new StringTokenizer(data);
    chunk = tokenizer.next('\n');
    if (chunk.compareTo("OFF") != 0) {
      throw new UnsupportedOperationException("Ill-formatted OFF data read: " + chunk);
    }

    tokie = new StringTokenizer(tokenizer.next('\n'));
    nVerts = Integer.valueOf(tokie.next(' '));
    nFaces = Integer.valueOf(tokie.next(' '));

    MeshVertex verts[] = new MeshVertex[nVerts];

    for (int i = 0; i < nVerts; i++)
    {
      tokie = new StringTokenizer(tokenizer.next('\n'));
      double x = Double.valueOf(tokie.next(' '));
      double y = Double.valueOf(tokie.next(' '));
      double z = Double.valueOf(tokie.next(' '));

      verts[i] = new MeshVertex(new Vec3(x, y, z));
    }
    for (int i = 0; i<nFaces; i++)
    {
      tokie = new StringTokenizer(tokenizer.next('\n'));
      int n = Integer.valueOf(tokie.next(' '));
      MeshVertex faceVerts[] = new MeshVertex[n];

      for (int j = 0; j < n; j++) {
        String tok = tokie.next(' ');
        int index = Integer.valueOf(tok);
        faceVerts[j] = verts[index];
      }
      mesh.add(new MeshFace(faceVerts));
    }

    mesh.computeAllNormals();
    return mesh;
  }

  public static void write(Mesh mesh, String filename) {
    try {
      final BiMap<MeshVertex, Integer> indices = HashBiMap.create();
      BufferedWriter out = new BufferedWriter(new FileWriter(filename));
      out.write("OFF\n");
      int i = 0;
      for (MeshVertex v : mesh.getVertices()) {
        indices.put(v, i++);
      }
      out.write(indices.size() + " " + mesh.size() + " 0\n");
      for (i = 0; i < indices.size(); i++) {
        MeshVertex v = indices.inverse().get(i);
        out.write(((float) v.getX()) + " " + ((float) v.getY()) + " " + ((float) v.getZ()) + "\n");
      }
      for (MeshFace f : mesh) {
        out.write(f.size() + " ");
        out.write(Joiner.on(" ").join(FluentIterable.from(f).transform(
            new Function<MeshVertex, Integer>() { 
              @Override 
              public Integer apply(MeshVertex v) {
                return indices.get(v);
              }
            })));
        out.write("\n");
      }
      out.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
