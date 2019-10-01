package com.bentonian.framework.io;

import java.util.prefs.Preferences;

public class PreferenceStore {

  public static void put(Class<?> clazz, String name, String value) {
    try {
      Preferences prefs = Preferences.userNodeForPackage(clazz);
      prefs.put(name, (value == null) ? "" : value);
    } catch (Exception e) {
      System.err.println(e.getMessage());
    }
  }

  public static String get(Class<?> clazz, String name) {
    try {
      Preferences prefs = Preferences.userNodeForPackage(clazz);
      return prefs.get(name, null);
    } catch (Exception e) {
      System.err.println(e.getMessage());
      return null;
    }
  }
}
