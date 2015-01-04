package com.bentonian.framework.io;

public class StringTokenizer {

  private final String buffer;

  private int offset = 0;

  public StringTokenizer(String buffer) {
    this.buffer = buffer;
  }

  public boolean isspace(char c) {
    return (c == ' ') || (c == '\n') || (c == '\t');
  }

  public String getBuffer() {
    return buffer;
  }

  public String next(char token) {
    String ret = new String();

    while (offset < buffer.length() && (isspace(buffer.charAt(offset)) || buffer.charAt(offset) == '#')) {
      while (offset < buffer.length() && isspace(buffer.charAt(offset))) {
        offset++;
      }
      if (offset < buffer.length() && buffer.charAt(offset) == '#') {
        while (offset < buffer.length() && buffer.charAt(offset) != '\n') {
          offset++;
        }
      }
    }

    while (offset < buffer.length() && buffer.charAt(offset) != token) {
      ret = ret + buffer.charAt(offset++);
    }
    if (offset < buffer.length()) {
      offset++;
    }
    return ret.trim();
  }
}
