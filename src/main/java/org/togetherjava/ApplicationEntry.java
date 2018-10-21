package org.togetherjava;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import org.togetherjava.config.ConfigValidator;

public class ApplicationEntry {

  public static void main(String[] args) {
    String configPath = Objects
        .requireNonNull(fetchConfigPath(args), "Config path not found");

    File configFile = new File(configPath);
    if (!configFile.exists()) {
      System.err.printf("Config '%s' does not exist!%n", configFile.getAbsolutePath());
      System.exit(1);
    }

    Toml toml = new Toml()
        .read(new File(configPath));

    new ConfigValidator().validateConfig(toml);

    try {
      new TogetherJavaBot(toml).start();
    } catch (InterruptedException | LoginException e) {
      System.err.println("An error occurred starting the bot.");
      e.printStackTrace();
      System.exit(2);
    }
  }

  private static String fetchConfigPath(String[] args) {
    if (args.length == 0) {
      System.err.println("Looking for a config in the 'TJ_CONFIG_PATH' env variable...");
      return System.getenv("TJ_CONFIG_PATH");
    }

    System.err.println("Treating the first argument as the config path");
    return args[0];
  }
}
