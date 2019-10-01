package com.bentonian.framework.ui;

import static com.bentonian.framework.ui.ShaderUtil.compileProgram;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bentonian.framework.io.FileUtil;

public class ShaderAutoloader {

  private volatile long lastFailedTimestamp = 0;
  private volatile long lastLoadedTimestamp = 0;

  private final String[] directoriesToMonitor;
  private final Supplier<Integer> vs;
  private final Supplier<Integer> fs;
  private final Supplier<Boolean> exitRequested;
  private final Consumer<Integer> successHandler;
  private final Consumer<String> errorHandler;

  public ShaderAutoloader(String[] directoriesToMonitor, Supplier<Integer> vs, Supplier<Integer> fs,
      Supplier<Boolean> exitRequested, Consumer<Integer> successHandler, Consumer<String> errorHandler) {
    this.directoriesToMonitor = directoriesToMonitor;
    this.vs = vs;
    this.fs = fs;
    this.exitRequested = exitRequested;
    this.successHandler = successHandler;
    this.errorHandler = errorHandler;
    new Reloader().start();
  }

  public void preDraw() {
    if (lastLoadedTimestamp == 0) {
      updateProgram();
    }
  }

  public void updateProgram() {
    lastLoadedTimestamp = -1;
    lastFailedTimestamp = getCurrentShaderTimestamp();
    try {
      successHandler.accept(compileProgram(vs.get(), fs.get()));
      lastLoadedTimestamp = getCurrentShaderTimestamp();
      lastFailedTimestamp = 0;
    } catch (RuntimeException e) {
      errorHandler.accept(e.getClass().getSimpleName() + ": " + e.getMessage());
    }
  }

  private synchronized long getCurrentShaderTimestamp() {
    long stamp = 0;
    for (String path : directoriesToMonitor) {
      stamp ^= FileUtil.getDirectoryTimestampHash(path);
    }
    return stamp;
  }

  private class Reloader extends Thread {
    @Override
    public void run() {
      while (!exitRequested.get()) {
        if (lastLoadedTimestamp != 0 || lastFailedTimestamp != 0) {
          long timestamp = getCurrentShaderTimestamp();
          if (timestamp != -1) {
            if ((lastFailedTimestamp != 0 && lastFailedTimestamp != timestamp)
                || (lastFailedTimestamp == 0 && lastLoadedTimestamp != timestamp)) {
              lastLoadedTimestamp = 0;
              lastFailedTimestamp = 0;
            }
          }
        }
        try {
          Thread.sleep(500);
        } catch (InterruptedException e) {
        }
      }
    }
  }

}
