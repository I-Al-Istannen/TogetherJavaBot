package org.togetherjava.config;

import com.moandjiezana.toml.Toml;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ConfigValidator {

  private Toml defaultConfig;

  public ConfigValidator() {
    defaultConfig = new Toml().read(getClass().getResourceAsStream("/config.toml"));
  }

  /**
   * Validates the given config.
   *
   * @param config the config to validate
   * @throws ConfigValidationException if the config is not valid
   */
  public void validateConfig(Toml config) {
    Map<String, Class<?>> defaultEntries = findEntries(defaultConfig, "");
    Map<String, Class<?>> passedEntries = findEntries(config, "");

    for (Entry<String, Class<?>> entry : defaultEntries.entrySet()) {
      String path = entry.getKey();

      if (!passedEntries.containsKey(path)) {
        throw new ConfigValidationException("Config does not contain key '%s'.", path);
      }

      if (passedEntries.get(path) != defaultEntries.get(path)) {
        throw new ConfigValidationException(
            "Key '%s' should be of type '%s', but was '%s'",
            path, defaultEntries.get(path).getSimpleName(), passedEntries.get(path).getSimpleName()
        );
      }
    }
  }

  private Map<String, Class<?>> findEntries(Toml input, String prefix) {
    Map<String, Class<?>> entries = new HashMap<>();

    for (Entry<String, Object> entry : input.entrySet()) {
      Class<?> valueType = entry.getValue().getClass();

      if (!(entry.getValue() instanceof Toml)) {
        entries.put(prefix + entry.getKey(), valueType);
      } else {
        entries.putAll(
            findEntries(
                (Toml) entry.getValue(), prefix + entry.getKey() + "."
            )
        );
      }
    }

    return entries;
  }

  public static class ConfigValidationException extends RuntimeException {

    public ConfigValidationException(String message) {
      super(message);
    }

    public ConfigValidationException(String message, Object... formatArgs) {
      super(String.format(message, formatArgs));
    }
  }
}
