package org.togetherjava;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.security.auth.login.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togetherjava.config.ConfigValidator;

public class ApplicationEntry {

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationEntry.class);

  private static final String TOKEN_ENV = "TJ_TOKEN";
  private static final String CONFIG_ENV = "TJ_CONFIG_PATH";

  public static void main(String[] args) {
    String configPath = Objects
        .requireNonNull(fetchConfigPath(args), "Config path not found");

    final String token = Objects.requireNonNull(fetchToken(), "Token not found.");

    File configFile = new File(configPath);
    if (!configFile.exists()) {
      LOGGER.error(
          "Config '{}' does not exist, but I hopefully created it!"
              + " Check and edit it, if you wish and then restart me.",
          configFile.getAbsolutePath()
      );
      copyDefaultConfig(configFile.toPath());
      System.exit(1);
    }

    Toml toml = new Toml()
        .read(new File(configPath));

    new ConfigValidator().validateConfig(toml);

    try {
      new TogetherJavaBot(toml).start(token);
    } catch (InterruptedException | LoginException e) {
      LOGGER.error("An error occurred starting the bot.", e);
      System.exit(2);
    }
  }

  /**
   * Attempts to read the bot token from an environment variable.
   *
   * @return the token
   */
  private static String fetchToken() {
    LOGGER.info("Looking for bot token in the '{}' environment variable...", TOKEN_ENV);
    return System.getenv(TOKEN_ENV);
  }

  /**
   * Attempts to read the config path from an environment variable, or from the command line
   * arguments.
   *
   * @param args the program arguments
   * @return the config file path
   */
  private static String fetchConfigPath(String[] args) {
    if (args.length == 0) {
      LOGGER.info("Looking for a config path in the '{}' environment variable...", CONFIG_ENV);
      return System.getenv(CONFIG_ENV);
    }

    LOGGER.info("Treating the first argument as the config path");
    return args[0];
  }

  private static void copyDefaultConfig(Path configPath) {
    try (OutputStream outputStream = Files.newOutputStream(configPath);
        InputStream inputStream = ApplicationEntry.class.getResourceAsStream("/config.toml")) {

      inputStream.transferTo(outputStream);
    } catch (IOException e) {
      LOGGER.error("Error writing default config to " + configPath.toAbsolutePath(), e);
    }
  }
}
