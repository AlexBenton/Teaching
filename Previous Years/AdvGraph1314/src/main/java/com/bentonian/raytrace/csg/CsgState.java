package com.bentonian.raytrace.csg;

public enum CsgState {
  InA {
    @Override CsgState toggleA() { return InNeither; }
    @Override CsgState toggleB() { return InBoth; }
    @Override boolean inA()      { return true; }
    @Override boolean inB()      { return false; }
  },
  InB {
    @Override CsgState toggleA() { return InBoth; }
    @Override CsgState toggleB() { return InNeither; }
    @Override boolean inA()      { return false; }
    @Override boolean inB()      { return true; }
  },
  InBoth {
    @Override CsgState toggleA() { return InB; }
    @Override CsgState toggleB() { return InA; }
    @Override boolean inA()      { return true; }
    @Override boolean inB()      { return true; }
  },
  InNeither {
    @Override CsgState toggleA() { return InA; }
    @Override CsgState toggleB() { return InB; }
    @Override boolean inA()      { return false; }
    @Override boolean inB()      { return false; }
  };

  abstract CsgState toggleA();
  abstract CsgState toggleB();
  abstract boolean inA();
  abstract boolean inB();
  
  public static CsgState get(boolean A, boolean B) {
    return A ? (B ? InBoth : InA) : (B ? InB : InNeither);
  }
}
