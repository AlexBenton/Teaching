package com.bentonian.framework.ui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

public class GLWindowedApp extends GLWindowedCanvas implements Runnable {
  
  protected boolean exitRequested = false;
  
  public void requestExit() {
    exitRequested = true;
  }
  
  public boolean isExitRequested() {
    return exitRequested;
  }

  @Override
  public void run() {
    initGl();
    while (!Display.isCloseRequested() && !exitRequested) {
      mainLoop();
    }
    shutdownGl();
    System.exit(0);
  }

  @Override
  protected void onKeyDown(int key) {
    switch (key) {
    case Keyboard.KEY_ESCAPE:
      exitRequested = true;
      break;
    }
  }
}
