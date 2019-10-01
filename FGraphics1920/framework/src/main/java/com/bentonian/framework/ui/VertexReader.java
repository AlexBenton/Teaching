package com.bentonian.framework.ui;


public interface VertexReader {
  public float[] asFloats(Vertex v);
  
  public static final VertexReader POSITION_READER = new VertexReader() {
    @Override public float[] asFloats(Vertex v) { return v.asFloats(); }
  };

  public static final VertexReader NORMAL_READER = new VertexReader() {
    @Override public float[] asFloats(Vertex v) { return v.getNormal().asFloats(); }
  };
  
  public static final VertexReader COLOR_READER = new VertexReader() {
    @Override public float[] asFloats(Vertex v) { return v.getColor().asFloats(); }
  };
  
  public static final VertexReader TEXTURE_COORDS_READER = new VertexReader() {
    @Override public float[] asFloats(Vertex v) { return v.getTexCoords().asFloats(); }
  };
}
