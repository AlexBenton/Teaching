package com.bentonian.framework.ui;

import com.bentonian.framework.io.SimpleFrame;

public class GLWindowedAppSecondaryFrame extends SimpleFrame {

  private final GLFWCanvas app;

  public GLWindowedAppSecondaryFrame(GLFWCanvas app, String title) {
    super(title);
    this.app = app;
  }
  
  @Override
  protected void onClose() {
    app.requestExit();
  }
}
