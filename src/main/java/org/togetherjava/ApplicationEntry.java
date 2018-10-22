package org.togetherjava;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import org.togetherjava.config.ConfigValidator;

public class ApplicationEntry {

  private static final String TOKEN_ENV = "TJ_TOKEN";
  private static final String CONFIG_ENV = "TJ_CONFIG_PATH";

  public static void main(String[] args) {
    String configPath = Objects
        .requireNonNull(fetchConfigPath(args), "Config path not found");

    final String token = Objects.requireNonNull(fetchToken(), "Token not found.");

    File configFile = new File(configPath);
    if (!configFile.exists()) {
      System.err.printf("Config '%s' does not exist!%n", configFile.getAbsolutePath());
      System.exit(1);
    }

    Toml toml = new Toml()
        .read(new File(configPath));

    new ConfigValidator().validateConfig(toml);

    try {
      new TogetherJavaBot(toml).start(token);
    } catch (InterruptedException | LoginException e) {
      System.err.println("An error occurred starting the bot.");
      e.printStackTrace();
      System.exit(2);
    }
  }
  
  /**
   * Attempts to read the bot token from an environment variable.
   * @return the token
   */
  private static String fetchToken() {
    System.err.println("Looking for bot token in the '" + TOKEN_ENV + "' environment variable...");
    return System.getenv(TOKEN_ENV);
  }

  /**
   * Attempts to read the config path from an environment variable,
   * or from the command line arguments.
   * @param args
   * @return the config file path
   */
  private static String fetchConfigPath(String[] args) {
    if (args.length == 0) {
      System.err.println("Looking for a config in the '" + CONFIG_ENV + "' environment variable...");
      return System.getenv(CONFIG_ENV);
    }

    System.err.println("Treating the first argument as the config path");
    return args[0];
  }
}
