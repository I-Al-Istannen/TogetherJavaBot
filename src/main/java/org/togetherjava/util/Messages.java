package org.togetherjava.util;

import java.util.Map;
import java.util.Optional;
import org.yaml.snakeyaml.Yaml;

public class Messages {

  private Map<String, Object> map;

  public Messages() {
    map = new Yaml().load(getClass().getResourceAsStream("/messages.yml"));
  }

  /**
   * Translates a string.
   *
   * @param key the key
   * @param formatArgs the format arguments
   * @return tbe translated string
   * @throws IllegalArgumentException if the key was not found
   */
  public String tr(String key, Object... formatArgs) {
    String storedValue = implGet(key, map);
    if (storedValue == null) {
      throw new IllegalArgumentException("Path not found: '" + key + "'");
    }
    return String.format(storedValue, formatArgs);
  }

  /**
   * Translates a string.
   *
   * @param key the key
   * @param formatArgs the format arguments
   * @return tbe translated string
   */
  public Optional<String> trOptional(String key, Object... formatArgs) {
    String storedValue = implGet(key, map);
    if (storedValue == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(String.format(storedValue, formatArgs));
  }

  /**
   * Checks if the messages has a given key.
   *
   * @param key the key
   * @return true if the kex exists.
   */
  public boolean hasKey(String key) {
    return implGet(key, map) != null;
  }

  private String implGet(String path, Map<String, Object> root) {
    String firstPart = path.split("\\.")[0];

    if (!root.containsKey(firstPart)) {
      return null;
    }
    Object fetched = root.get(firstPart);

    if (path.length() == firstPart.length()) {
      return fetched.toString();
    }

    String restPath = path.substring(path.indexOf('.') + 1);

    @SuppressWarnings("unchecked")
    Map<String, Object> nestedMap = (Map<String, Object>) fetched;
    return implGet(restPath, nestedMap);
  }
}
