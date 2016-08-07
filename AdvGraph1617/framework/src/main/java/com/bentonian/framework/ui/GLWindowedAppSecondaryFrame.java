package com.bentonian.framework.ui;

import com.bentonian.framework.io.SimpleFrame;

public class GLWindowedAppSecondaryFrame extends SimpleFrame {

  private final GLWindowedApp app;

  public GLWindowedAppSecondaryFrame(GLWindowedApp app, String title) {
    super(title);
    this.app = app;
  }
  
  @Override
  protected void onClose() {
    app.requestExit();
  }
}
