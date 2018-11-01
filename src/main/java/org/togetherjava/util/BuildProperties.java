package org.togetherjava.util;

import java.io.IOException;
import java.util.Properties;

public class BuildProperties {

  private static final Properties PROPERTIES;

  static {
    PROPERTIES = new Properties();
    try {
      PROPERTIES.load(BuildProperties.class.getResourceAsStream("/build.properties"));
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize build properties", e);
    }
  }

  public static String getVersion() {
    return PROPERTIES.getProperty("project.version");
  }

  public static String getGitCommitId() {
    return PROPERTIES.getProperty("git.commit.id.abbrev");
  }

  public static String getGitCommitTime() {
    return PROPERTIES.getProperty("git.commit.time");
  }

  public static String getGitBranch() {
    return PROPERTIES.getProperty("git.branch");
  }

  public static boolean isGitDirty() {
    return Boolean.parseBoolean(PROPERTIES.getProperty("git.dirty"));
  }
}
